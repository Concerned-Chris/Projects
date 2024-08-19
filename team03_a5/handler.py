import bz2
# import xml.sax
import xml.sax.xmlreader
from xml.sax import make_parser, handler
from xml.sax.saxutils import escape
import requests
from rdflib import URIRef, BNode, Literal, Namespace, Graph
from rdflib.namespace import FOAF, XSD, RDF
import http.client
import urllib.parse
import os
from pymantic import sparql
import xml.etree.ElementTree as ET
from lxml import etree
import sys

http.client.HTTPConnection._http_vsn = 10
http.client.HTTPConnection._http_vsn_str = 'HTTP/1.0'


class Problem:
    prob_id = 0
    problem_type = ""
    of = ""
    mscs = []
    after = ""
    before = ""
    problem_from = ""
    problem_to = ""
    with_query = False
    query = ""
    solution = []

    def __init__(self, pid):
        self.prob_id = pid

    def set_mscs(self, m):
        self.mscs = m

    def set_solution(self, sol):
        self.solution = sol


class Entry:
    documentId = ""
    classifications = []
    authorIds = []
    keywords = []
    publicationYear = ""

    def __init__(self, d, c, a, k, p):
        self.documentId = d
        self.classifications = c
        self.authorIds = a
        self.keywords = k
        self.publicationYear = p


class XMLHandler(handler.ContentHandler):
    def __init__(self):
        self.CurrentData = ""
        self.documentId = ""
        self.classifications = []
        self.authorIds = []
        self.keywords = []
        self.publicationYear = ""
        self.entries = []

    # Call when an element starts
    def startElement(self, tag, attributes):
        self.CurrentData = tag

    # Call when an elements ends
    def endElement(self, tag):
        if self.CurrentData == "zbmath:serial_title":
            e = Entry(self.documentId, self.classifications, self.authorIds, self.keywords, self.publicationYear)
            self.documentId = ""
            self.classifications = []
            self.authorIds = []
            self.keywords = []
            self.publicationYear = ""
            self.entries.append(e)
        self.CurrentData = ""

    def characters(self, content):
        if self.CurrentData == "zbmath:document_id":
            self.documentId = content
        elif self.CurrentData == "zbmath:author_id":
            self.authorIds.append(content)
        elif self.CurrentData == "zbmath:classification":
            self.classifications.append(content)
        elif self.CurrentData == "zbmath:keyword":
            self.keywords.append(content)
        elif self.CurrentData == "zbmath:publication_year":
            self.publicationYear = content

    def get_number_entries(self):
        return len(self.entries)

    def get_entries(self, start, end):
        return self.entries[start:end]


def parse_to_url(query):
    return urllib.parse.quote_plus(query).replace("+", "%20")


def parse(filename):
    parser = make_parser()
    # parser.setFeature(xml.sax.handler.feature_namespaces, "zbmath")

    handler = XMLHandler()
    parser.setContentHandler(handler)

    namespace = Namespace("http://www.aisysproj.kwarc.info/")
    g = Graph()
    g.bind("foaf", FOAF)
    g.bind("xsd", XSD)
    g.bind("ai1", namespace)

    # first 22 chars have to be skipped
    with bz2.open(filename) as fp:
        fp.read(22)
        # parser.parse(fp)
        count = 1
        while True:
            print(count)

            #data = fp.read(67108864) => 283
            data = fp.read(100000000)
            #data = fp.read(100000)
            if not data:
                break
            count = count + 1

            parser.feed(data)

    number_entries = handler.get_number_entries()
    print(number_entries)
    i = 0
    count = 1
    while(i < number_entries):
        entries = handler.get_entries(i, i + 100000)
        g = Graph()
        g.bind("foaf", FOAF)
        g.bind("xsd", XSD)
        g.bind("ai1", namespace)
        for entry in entries:
            report = URIRef("http://www.aisysproj.kwarc.info/report:" + entry.documentId)
            docId = Literal(entry.documentId)
            pubYear = Literal(entry.publicationYear, datatype=XSD.integer)

            g.add((report, RDF.type, namespace.report))
            g.add((report, namespace.documentId, docId))
            g.add((report, namespace.publificationYear, pubYear))
            for c in entry.classifications:
                g.add((report, namespace.classification, Literal(c)))
            for a in entry.authorIds:
                g.add((report, namespace.authorId, Literal(a)))
            for k in entry.keywords:
                g.add((report, namespace.keyword, Literal(k)))
        # print(len(g))
        destination = 'triples' + str(count) + '.ttl'
        g.serialize(destination=destination, format='turtle')
        i = i + 100000
        count = count + 1

    #parser.close()


def execute_post_request(url_encoded_request):
    URL = "http://localhost:9999/blazegraph/namespace/kb/sparql?update="
    URL = URL + url_encoded_request
    # print(URL)
    response = requests.post(URL)
    # print(response)
    return response

def load_into_blazegraph(filenumbers):
    print("drop")
    drop = parse_to_url("DROP ALL")
    execute_post_request(drop)
    for n in filenumbers:
        print(n)
        #load = "LOAD <file:///E:/Uni/AI1Projekt/team03/triples" + str(n) + ".ttl>"
        parts = os.path.abspath(os.getcwd()).split('\\')
        load = "LOAD <file:///"
        for p in parts:
            load = load + p + "/"
        load = load + "triples"
        load = load + str(n) + ".ttl>"
        insert = parse_to_url(load)
        response = execute_post_request(insert)
        #print(response)


def execute_get_request(url_encoded_query):
    URL = "http://localhost:9999/blazegraph/namespace/kb/sparql?query="
    URL = URL + url_encoded_query
    # print(URL)
    header = {'Accept': 'application/json'}
    r = requests.get(URL, headers=header)
    data = r.json()
    return data

def read_problem(filename):
    problems = []
    tree = ET.parse(filename)
    root = tree.getroot()
    for problem in root.iter('Problem'):
        prob = Problem(problem.get('id'))
        prob.problem_type = problem.get('type')
        vals = problem.findall('value')
        if problem.get('withquery') == "true":
            prob.with_query = True
        if prob.problem_type == "coauthors":
            for v in vals:
                prob.of = v.text[35:]
        elif prob.problem_type == "msc-intersection":
            m = []
            for v in vals:
                m.append(v.text[42:])
            prob.set_mscs(m)
        elif prob.problem_type == "top-3-keywords":
            for v in vals:
                if v.get('type') == "of":
                    prob.of = v.text[35:]
                elif v.get('type') == "before":
                    prob.before = v.text
                else:
                    prob.after = v.text
        else:
            for v in vals:
                if v.get('type') == "from":
                    prob.problem_from = v.text[35:]
                else:
                    prob.problem_to = v.text[35:]
        problems.append(prob)
    return problems


def solve_problems(problems, start, end):
    for i in range(start-1, end):
        prob = problems[i]
        query = ""
        write_query = ""
        print(i+1)
        if prob.problem_type == "coauthors":
            query = "SELECT DISTINCT ?aut "
            query = query + "WHERE {?doc <http://www.aisysproj.kwarc.info/authorId> "
            query = query + "'" + prob.of + "'."
            query = query + " ?doc <http://www.aisysproj.kwarc.info/authorId> ?aut}"
            query = query + " ORDER BY ?aut"
            write_query = escape(query)
            query = parse_to_url(query)
            data = execute_get_request(query)
            sol = []
            for e in data['results']['bindings']:
                if e['aut']['value'] != prob.of:
                    sol.append(e['aut']['value'])
            prob.set_solution(sol)
        elif prob.problem_type == "msc-intersection":
            query = "SELECT ?id "
            query = query + "WHERE {?s <http://www.aisysproj.kwarc.info/documentId> ?id. "
            query = query + "?s <http://www.aisysproj.kwarc.info/classification> ?class. "
            sets = []
            for msc in prob.mscs:
                exQuery = query + "FILTER( regex(?class, \"" + msc + "\"))}"
                write_query = write_query + escape(exQuery) + "\n"
                exQuery = parse_to_url(exQuery)
                data = execute_get_request(exQuery)
                newSet = set()
                for e in data['results']['bindings']:
                    newSet.add(e['id']['value'])
                sets.append(newSet)
            inter = sets[0].intersection(sets[1])
            if len(prob.mscs) == 3:
                inter = inter.intersection(sets[2])
            sol = []
            for it in inter:
                query = "SELECT ?class WHERE {?s <http://www.aisysproj.kwarc.info/documentId> '" + it + "'."
                query = query + " ?s <http://www.aisysproj.kwarc.info/classification> ?class. }"
                write_query = write_query + escape(query) + "\n"
                query = parse_to_url(query)
                data = execute_get_request(query)
                classes = set()
                for d in data['results']['bindings']:
                    classes.add(d['class']['value'])
                sat = [False] * len(prob.mscs)
                for c in classes:
                    for m in range(0, len(prob.mscs)):
                        if c.startswith(prob.mscs[m]):
                            sat[m] = True
                if sat == [True] * len(prob.mscs):
                    sol.append(it)
            prob.set_solution(sol)
        elif prob.problem_type == "top-3-keywords":
            query = "SELECT ?keyword (COUNT(?keyword) AS ?count) "
            query = query + "WHERE {?s <http://www.aisysproj.kwarc.info/authorId> "
            query = query + "'" + prob.of + "' ."
            query = query + "?s <http://www.aisysproj.kwarc.info/keyword> ?keyword. "
            query = query + "?s <http://www.aisysproj.kwarc.info/publificationYear> ?year. "
            if prob.before:
                query = query + "FILTER (?year < " + prob.before + "). "
            if prob.after:
                query = query + "FILTER (?year > " + prob.after + "). "
            query = query + "} GROUP BY ?keyword"
            write_query = escape(query)
            data = execute_get_request(query)
            ls = []
            for e in data['results']['bindings']:
                ls.append((e['keyword']['value'], int(e['count']['value'])))
            ls = sorted(ls, key=lambda x: x[1], reverse=True)
            #print(ls)
            prob.set_solution(ls[:3])
        else:
            turn = 0
            docid = "<http://www.aisysproj.kwarc.info/documentId>"
            autid = "<http://www.aisysproj.kwarc.info/authorId>"
            limit = "LIMIT 1"
            p1 = "?p1"
            id1 = "?id1"
            select = "SELECT ?id1"
            where = " WHERE { " + p1 + " " + docid + " " + id1 + ". "
            where = where + p1 + " " + autid + " '" + prob.problem_from + "' ."
            where = where + " " + p1 + " " + autid
            query = select + where + " '" + prob.problem_to + "' }" + " " + limit
            #print(query)
            query = parse_to_url(query)
            data = execute_get_request(query)
            filterq = ""
            f = "FILTER (?"
            un = " != "
            while not data['results']['bindings']:
                turn = turn + 1
                select = select + " ?a" + str(turn+1) + " ?id" + str(turn+1)
                where = where + " ?a" + str(turn+1) + ". "
                where = where + "?p" + str(turn+1) + " " + docid + " ?id" + str(turn+1) + ". "
                where = where + "?p" + str(turn+1) + " " + autid + " ?a" + str(turn+1) + ". "
                where = where + "?p" + str(turn+1) + " " + autid
                if turn > 1:
                    filterq = filterq + f + "a" + str(turn) + un + "?a" + str(turn+1) + "). "
                filterq = filterq + f + "p" + str(turn) + un + "?p" + str(turn+1) + "). "
                query = select + where + " '" + prob.problem_to + "'. " + filterq + " } " + limit
                #print(query)
                query = parse_to_url(query)
                data = execute_get_request(query)
            sol = []
            d = data['results']['bindings'][0]
            sol.append(prob.problem_from)
            sol.append(d['id1']['value'])
            #print(data)
            r = 2
            while r <= turn+1:
                sol.append(d['a' + str(r)]['value'])
                sol.append(d['id' + str(r)]['value'])
                r = r + 1
            sol.append(prob.problem_to)
            prob.set_solution(sol)
            """
            query1 = "SELECT DISTINCT ?co ?id "
            query1 = query1 + "WHERE {?s <http://www.aisysproj.kwarc.info/authorId> '"
            query2 = "' ." + "?s <http://www.aisysproj.kwarc.info/authorId> ?co. "
            query2 = query2 + "?s <http://www.aisysproj.kwarc.info/documentId> ?id }"
            query2 = query2 + "ORDER BY ?co"
            neighbors = set()
            id_map = {}
            pred = {}
            query = query1 + prob.problem_from + query2
            query = parse_to_url(query)
            data = execute_get_request(query)
            for d in data['results']['bindings']:
                if not d['co']['value'] in neighbors:
                    id_map[(prob.problem_from, d['co']['value'])] = d['id']['value']
                    pred[d['co']['value']] = prob.problem_from
                    neighbors.add(d['co']['value'])
            found = neighbors.copy()
            #print("count neighbors: " + str(len(neighbors)))
            # print(found)
            round = 1
            while not prob.problem_to in found:
                print("Round: " + str(round))
                neighbors_new = set()
                for n in neighbors:
                    #print("neighbor: " + n)
                    query = query1 + n + query2
                    query = parse_to_url(query)
                    data = execute_get_request(query)
                    for d in data['results']['bindings']:
                        if not d['co']['value'] in found:
                            id_map[(n, d['co']['value'])] = d['id']['value']
                            pred[d['co']['value']] = n
                            neighbors_new.add(d['co']['value'])
                    # print(neighbors_new)
                    found.update(neighbors_new)
                neighbors = neighbors_new
                round = round + 1
            current = prob.problem_to
            loes = []
            loes.insert(0, current)
            while not pred.get(current) == prob.problem_from:
                cur_pred = pred.get(current)
                cur_id = id_map.get((cur_pred, current))
                # print(cur_pred + "-" + str(cur_id) + "->" + current)
                current = cur_pred
                loes.insert(0, cur_id)
                loes.insert(0, cur_pred)
            cur_id = id_map.get((prob.problem_from, current))
            # print(prob.problem_from + "-" + str(cur_id) + "->" + current)
            loes.insert(0, cur_id)
            loes.insert(0, prob.problem_from)
            prob.set_solution(loes)
            """
        if prob.with_query:
            prob.query = write_query

    return problems


# https://www.geeksforgeeks.org/reading-and-writing-xml-files-in-python/
def write_solution(problems):
    root = ET.Element('Solutions')
    for prob in problems:
        sol = ET.SubElement(root, 'Solution')
        sol.set('id', prob.prob_id)
        if prob.problem_type == "coauthors":
            for s in prob.solution:
                val = ET.SubElement(sol, 'value')
                val.set('type', 'coauthor')
                val.text = "https://zbmath.org/authors/?q=ai%3A" + s
        elif prob.problem_type == "msc-intersection":
            for s in prob.solution:
                val = ET.SubElement(sol, 'value')
                val.set('type', 'paper')
                val.text = "https://zbmath.org/?q=an%3A" + s
        elif prob.problem_type == "top-3-keywords":
            for tup in prob.solution:
                val = ET.SubElement(sol, 'value')
                val.set('type', 'keyword')
                val.text = "https://zbmath.org/?q=an%3A" + escape(tup[0])
                val2 = ET.SubElement(sol, 'value')
                val2.set('type', 'count')
                val2.text = str(tup[1])
        else:
            for s in range(0, len(prob.solution)):
                val = ET.SubElement(sol, 'value')
                if s % 2 == 0:
                    val.set('type', 'author')
                    val.text = "https://zbmath.org/authors/?q=ai%3A" + escape(prob.solution[s])
                else:
                    val.set('type', 'paper')
                    val.text = "https://zbmath.org/?q=an%3A" + prob.solution[s]
        if prob.with_query:
            q = ET.SubElement(sol, 'query')
            q.text = prob.query

    tree = ET.ElementTree(root)
    ET.indent(root, '  ')
    tree.write("solution.xml", encoding="utf-8", xml_declaration=True)

    return


def main():
    #problems = read_problem("problems.xml")
    #solve_problems(problems, 16, 20)
    #write_solution(problems)
    #load_into_blazegraph(range(1,2))
    args = sys.argv[1:]
    mode = args[0]
    if mode == "parse":
        filename = args[1]
        parse(filename)
    elif mode == "load":
        start = args[1]
        end = args[2]
        numbers = range(start, end + 1)
        load_into_blazegraph(numbers)
    elif mode == "solve":
        if args[2] == "all":
            problems = read_problem(args[1])
            solve_problems(problems, 1, len(problems))
            write_solution(problems)
        else:
            problems = read_problem(args[1])
            solve_problems(problems, args[2], args[3])
            write_solution(problems)
    else:
        print("Please enter a valid mode1")


    #parse("mini-dataset.xml.bz2")
    #parse("zbMathOpen_OAIPMH_int.xml.bz2")

    # encode = parse_to_url("LOAD <file:///" + os.path.abspath("rows.rdf") + ">")
    # encode = parse_to_url("LOAD <file:///" + os.path.abspath("example.rdf") + ">")

    #encode = parse_to_url("LOAD <file:///E:/Uni/AI1Projekt/team03/triples_mini.ttl>")
    # encode = parse_to_url("LOAD <file:///E:/Uni/AI1Projekt/assignment5/log_sample.ttl>")
    # encode = parse_to_url("DROP ALL")
    # print(encode)
    #response = execute_post_request(encode)
    #print(response)
    #numbers = range(1, 43)
    #load_into_blazegraph(numbers)
    # query = parse_to_url("SELECT * WHERE {?s ?p ?o}")
    # execute_get_request(query)

    #problems = read_problem("example-problems.xml")
    #problems = solve_problems(problems, 1, 1)
    #write_solution(False, problems)

    # probs = read_problem("mini-problems.xml")


if __name__ == "__main__":
    main()
