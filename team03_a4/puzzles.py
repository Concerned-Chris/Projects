import datetime

import svgwrite
import os
import sys

def fill_part(length, fp):
    part = []
    for i in range(length):
        line = fp.readline().strip().split(' ')
        if line == [""]:
            line = []
        part.append(line)
    return part


def process_specification(filename):
    file_dir = os.path.dirname(os.path.realpath('__file__'))
    filename = 'clues/' + filename + ".clues"
    file_path = os.path.join(file_dir, filename)

    file_handle = open(file_path, 'r')
    start = file_handle.readline().strip()
    header = start.split(' ')
    colors = file_handle.readline().strip().split(' ')
    if header[0] == "rect":
        length = int(header[1])
        rows = fill_part(length, file_handle)
        length = int(header[2])
        columns = fill_part(length, file_handle)
        file_handle.close()
        return header, colors, rows, columns
    else:
        length = 2 * int(header[1]) - 1
        right = fill_part(length, file_handle)
        left_up = fill_part(length, file_handle)
        left_down = fill_part(length, file_handle)
        file_handle.close()
        return header, colors, right, left_up, left_down


def to_mask(mask, clue, size, init_size, number_colors, forbidden, masks):
    if not clue:
        if len(mask) <= init_size:
            masks.append(mask + "B" * (init_size - len(mask)))
        return masks
    if size < 0:
        return masks
    masks = to_mask(mask + "B", clue, size - 1, init_size, number_colors, None, masks)
    letter = clue[0][-1]
    freq = int(clue[0][:-1])
    new = letter * freq
    smaller = freq
    if letter == '?':
        letter_before = None
        if mask:
            letter_before = mask[-1]
        for i in range(1, number_colors + 1):
            if i == 1:
                letter = 'a'
                new = 'a' * freq
                new_forbidden = 'a'
            elif i == 2:
                letter = 'b'
                new = 'b' * freq
                new_forbidden = 'b'
            elif i == 3:
                letter = 'c'
                new = 'c' * freq
                new_forbidden = 'c'
            else:
                letter = 'd'
                new = 'd' * freq
                new_forbidden = 'd'
            if len(clue) > 1:
                next_letter = clue[1][-1]
                if letter == next_letter:
                    new = new + "B"
                    smaller = smaller + 1
            if letter == letter_before:
                continue
            if letter == forbidden:
                continue
            clue_copy = clue.copy()
            del clue_copy[0]
            masks = to_mask(mask + new, clue_copy, size - smaller, init_size, number_colors, new_forbidden, masks)
    else:
        if len(clue) > 1:
            next_letter = clue[1][-1]
            if letter == next_letter:
                new = new + "B"
                smaller = smaller + 1
        clue_copy = clue.copy()
        del clue_copy[0]
        masks = to_mask(mask + new, clue_copy, size - smaller, init_size, number_colors, None, masks)

    return masks


def to_mask_big_problem(mask, clue, size, init_size, number_colors, forbidden, masks):
    if not clue:
        if len(mask) <= init_size:
            mask = mask + "B" * (init_size - len(mask)) + "\n"
            fp = open("masks.txt", "a")
            fp.write(mask)
            fp.close()
        return masks + 1
    if size < 0:
        return masks
    masks = to_mask_big_problem(mask + "B", clue, size - 1, init_size, number_colors, None, masks)
    letter = clue[0][-1]
    freq = int(clue[0][:-1])
    new = letter * freq
    smaller = freq
    if letter == '?':
        letter_before = None
        if mask:
            letter_before = mask[-1]
        for i in range(1, number_colors + 1):
            if i == 1:
                letter = 'a'
                new = 'a' * freq
                new_forbidden = 'a'
            elif i == 2:
                letter = 'b'
                new = 'b' * freq
                new_forbidden = 'b'
            elif i == 3:
                letter = 'c'
                new = 'c' * freq
                new_forbidden = 'c'
            else:
                letter = 'd'
                new = 'd' * freq
                new_forbidden = 'd'
            if len(clue) > 1:
                next_letter = clue[1][-1]
                if letter == next_letter:
                    new = new + "B"
                    smaller = smaller + 1
            if letter == letter_before:
                continue
            if letter == forbidden:
                continue
            clue_copy = clue.copy()
            del clue_copy[0]
            masks = to_mask_big_problem(mask + new, clue_copy, size - smaller, init_size, number_colors, new_forbidden, masks)
    else:
        if len(clue) > 1:
            next_letter = clue[1][-1]
            if letter == next_letter:
                new = new + "B"
                smaller = smaller + 1
        clue_copy = clue.copy()
        del clue_copy[0]
        masks = to_mask_big_problem(mask + new, clue_copy, size - smaller, init_size, number_colors, None, masks)

    return masks


def var_map_rect(row_number, column_number, number_colors):
    var_map = {}
    for i in range(0, row_number):
        for j in range(1, column_number + 1):
            first = (((i * column_number) + j) * number_colors) - (number_colors - 1)
            ls = [first]
            for k in range(number_colors - 1):
                first = first + 1
                ls.append(first)
            var_map[(i + 1, j)] = ls
    return var_map


def var_map_hex(size, number_colors):
    var_map = {}
    var_count = 1
    start = (1, size, 2 * size - 1)
    for i in range(1, 2 * size):
        distance_to_size = abs(i - size)
        end = 2 * size - distance_to_size
        coord = start
        for j in range(1, end):
            vs = [var_count]
            var_count = var_count + 1
            for k in range(number_colors - 1):
                vs.append(var_count)
                var_count = var_count + 1
            var_map[coord] = vs
            coord = hex_r(coord)
        if i < size:
            start = hex_ld(start)
        else:
            start = hex_rd(start)
    return var_map


def hex_r(coord):
    return coord[0], coord[1] + 1, coord[2] - 1


def hex_rd(coord):
    return coord[0] + 1, coord[1], coord[2] - 1


def hex_ld(coord):
    return coord[0] + 1, coord[1] - 1, coord[2]


def hex_l(coord):
    return coord[0], coord[1] - 1, coord[2] + 1


def hex_lu(coord):
    return coord[0] - 1, coord[1], coord[2] + 1


def hex_ru(coord):
    return coord[0] - 1, coord[1] + 1, coord[2]


def make_coords_rect(kind, row_number, column_number, row_count, column_count):
    coords = []
    if kind == "row":
        for i in range(1, column_count + 1):
            coords.append((row_number, i))
    else:
        for i in range(1, row_count + 1):
            coords.append((i, column_number))
    return coords


def make_coords_hex(kind, row_number, column_number, column_number2, size):
    coords = []
    if kind == "row":
        start = (1, size, 2 * size - 1)
        for i in range(1, row_number):
            if i < size:
                start = hex_ld(start)
            else:
                start = hex_rd(start)
        coords.append(start)
        steps = (2 * size - 1) - abs(row_number - size)
        for i in range(1, steps):
            start = hex_r(start)
            coords.append(start)
    elif kind == "column":
        start = (2 * size - 1, 1, size)
        for i in range(1, column_number):
            if i < size:
                start = hex_r(start)
            else:
                start = hex_ru(start)
        coords.append(start)
        steps = (2 * size - 1) - abs(column_number - size)
        for i in range(1, steps):
            start = hex_lu(start)
            coords.append(start)
    else:
        start = (size, 2 * size - 1, 1)
        for i in range(1, column_number2):
            if i < size:
                start = hex_lu(start)
            else:
                start = hex_l(start)
        coords.append(start)
        steps = (2 * size - 1) - abs(column_number2 - size)
        for i in range(1, steps):
            start = hex_ld(start)
            coords.append(start)
    return coords


def make_dnf(masks, coords, var_dict):
    dnf = []
    for mask in masks:
        clause = []
        for c in range(0, len(coords)):

            varss = var_dict.get(coords[c])
            if mask[c] == 'B':
                varss = [-v for v in varss]
            elif mask[c] == 'a':
                varss = [-v for v in varss]
                varss[0] = -varss[0]
            elif mask[c] == 'b':
                varss = [-v for v in varss]
                varss[1] = -varss[1]
            elif mask[c] == 'c':
                varss = [-v for v in varss]
                varss[2] = -varss[2]
            elif mask[c] == 'd':
                varss = [-v for v in varss]
                varss[3] = -varss[3]
            for v in varss:
                clause.append(v)
        dnf.append(clause)
    return dnf


def make_cnf(dnf, aux_counter, filename):
    cnf, aux_counter = my_tseitin(dnf, aux_counter, filename)
    """
    max_val = np.max(dnf)
    cnf = Tseitin.Tseitin(dnf)
    remap = {}
    for i in range(len(cnf)):
        for j in range(len(cnf[i])):
            entry = cnf[i][j]
            if (entry > 0 and entry > max_val) or (entry < 0 and -entry > max_val):
                minus = False
                if entry < 0:
                    entry = -entry
                    minus = True
                if entry not in remap.keys():
                    aux_counter = aux_counter + 1
                    remap[entry] = aux_counter
                if minus:
                    cnf[i][j] = -remap.get(entry)
                else:
                    cnf[i][j] = remap.get(entry)
    """
    return cnf, len(cnf), aux_counter


def my_tseitin(dnf, aux_counter, filename):
    cnf = []
    aux_copy = aux_counter
    first = []
    for i in range(len(dnf)):
        aux_copy = aux_copy + 1
        first.append(aux_copy)
    cnf.append(first)
    for d in range(0, len(dnf)):
        for c in dnf[d]:
            clause = [-(aux_counter + d + 1), c]
            cnf.append(clause)
    aux_counter = aux_counter + len(dnf)
    write_cnf(cnf, filename)
    return cnf, aux_counter


def process_masks(masks, coords, var_dict, aux_counter, filename):
    print("masks:")
    print(len(masks))
    cc = 1
    aux_counter = aux_counter + 1
    file_dir = os.path.dirname(os.path.realpath('__file__'))
    fl = 'sat_files/' + filename + "_helper" + ".sat"
    file_path = os.path.join(file_dir, fl)
    fp = open(file_path, "a")
    for aux in range(aux_counter, aux_counter + len(masks)):
        fp.write(str(aux) + " ")
    fp.write("0\n")
    for a in range(aux_counter, aux_counter + len(masks)):
        mask = masks[a-aux_counter]
        for c in range(0, len(coords)):
            varss = var_dict.get(coords[c])
            if mask[c] == 'B':
                varss = [-v for v in varss]
            elif mask[c] == 'a':
                varss = [-v for v in varss]
                varss[0] = -varss[0]
            elif mask[c] == 'b':
                varss = [-v for v in varss]
                varss[1] = -varss[1]
            elif mask[c] == 'c':
                varss = [-v for v in varss]
                varss[2] = -varss[2]
            elif mask[c] == 'd':
                varss = [-v for v in varss]
                varss[3] = -varss[3]
            for v in varss:
                line = "-" + str(a) + " " + str(v) + " 0\n"
                fp.write(line)
                cc = cc + 1
                if cc % 1000000 == 0:
                    print(cc)
    aux_counter = aux_counter + len(masks) - 1
    fp.close()
    return cc, aux_counter


def process_masks_big(masks, coords, var_dict, aux_counter, filename):
    # print("masks:")
    # print(len(masks))
    f = open("masks.txt", "r")
    cc = 1
    aux_counter = aux_counter + 1
    file_dir = os.path.dirname(os.path.realpath('__file__'))
    fl = 'sat_files/' + filename + "_helper" + ".sat"
    file_path = os.path.join(file_dir, fl)
    fp = open(file_path, "a")
    for aux in range(aux_counter, aux_counter + masks):
        fp.write(str(aux) + " ")
    fp.write("0\n")
    for a in range(aux_counter, aux_counter + masks):
        mask = f.readline().strip()
        #TODO: why is this necessary?
        if len(mask) != len(coords):
            continue
        for c in range(0, len(coords)):
            varss = var_dict.get(coords[c])
            if mask[c] == 'B':
                varss = [-v for v in varss]
            elif mask[c] == 'a':
                varss = [-v for v in varss]
                varss[0] = -varss[0]
            elif mask[c] == 'b':
                varss = [-v for v in varss]
                varss[1] = -varss[1]
            elif mask[c] == 'c':
                varss = [-v for v in varss]
                varss[2] = -varss[2]
            elif mask[c] == 'd':
                varss = [-v for v in varss]
                varss[3] = -varss[3]
            for v in varss:
                line = "-" + str(a) + " " + str(v) + " 0\n"
                fp.write(line)
                cc = cc + 1
                if cc % 1000000 == 0:
                    print(cc)
    aux_counter = aux_counter + masks - 1
    fp.close()
    f.close()
    os.remove("masks.txt")
    return cc, aux_counter


def process_solution(filename, var_counter):
    file_dir = os.path.dirname(os.path.realpath('__file__'))
    fl = 'sat_solutions/' + filename + ".sol"
    file_path = os.path.join(file_dir, fl)
    fp = open(file_path, "r", encoding="utf16")
    solution = []
    length = 0
    line = fp.readline()
    #print(repr(line))
    while line != "s SATISFIABLE\n":
        #print(repr(line))
        line = fp.readline()

    while length < var_counter:
        trash = fp.read(2)
        line = fp.readline().strip().split(" ")
        #print(line)
        solution = solution + line
        length = len(solution)
        # solution = solution[0:var_counter]
    fp.close()
    return solution


def draw_rectangle(filename, var_map, colors, solution, number_rows, number_columns):
    file_dir = os.path.dirname(os.path.realpath('__file__'))
    fl = 'svgs/' + filename + ".svg"
    file_path = os.path.join(file_dir, fl)
    dwg = svgwrite.Drawing(file_path)
    coords = var_map.keys()
    for coord in coords:
        vals = var_map.get(coord)
        background = True
        count = 1
        color = ""
        for val in vals:
            if int(solution[val - 1]) > 0:
                color = colors[count]
                background = False
            count = count + 1
        if background:
            color = colors[0]
        dwg.add(dwg.rect(((coord[1] - 1) * 100, (coord[0] - 1) * 100), (100, 100),
                         stroke=svgwrite.rgb(10, 10, 16, '%'),
                         fill=color)
                )
    dwg.viewbox(minx=0, miny=0, width=(number_columns) * 100, height=(number_rows) * 100)
    dwg.save()


def hex_coords(top):
    hex_c = []
    hex_c.append(top)
    hex_c.append((top[0] + 100, top[1] + 50))
    hex_c.append((top[0] + 100, top[1] + 150))
    hex_c.append((top[0], top[1] + 200))
    hex_c.append((top[0] - 100, top[1] + 150))
    hex_c.append((top[0] - 100, top[1] + 50))
    # for elem in hex_c:
    # hex_c[hex_c.index(elem)] = elem[0]/10, elem[1]/10
    return hex_c


def draw_hexagon(filename, size, var_map, colors, solution):
    file_dir = os.path.dirname(os.path.realpath('__file__'))
    fl = 'svgs/' + filename + ".svg"
    file_path = os.path.join(file_dir, fl)
    dwg = svgwrite.Drawing(file_path)  # .viewbox(minx=0,miny=0,width=3000,height=3000)
    coords = var_map.keys()
    for coord in coords:
        vals = var_map.get(coord)
        background = True
        count = 1
        color = ""
        for val in vals:
            if int(solution[val - 1]) > 0:
                color = colors[count]
                background = False
            count = count + 1
        if background:
            color = colors[0]

        y = (150 * (coord[0] - 1))
        x = (200 * (coord[1] - size + 1) + 100 * coord[0])
        # - (2*size-1 - abs(size-coord[0])) * 100
        dwg.add(dwg.polygon(hex_coords((x, y)),
                            stroke=svgwrite.rgb(10, 10, 16, '%'),
                            fill=color)
                )
    """
    dwg.add(dwg.polygon(hex_coords((200, 150)),
                        stroke=svgwrite.rgb(10, 10, 16, '%'),
                        fill='blue')
            )
    """
    dwg.viewbox(minx=0, miny=0, width=(size * 2 - 1) * 200, height=(size * 2 - 1) * 200)
    dwg.save()


def draw_solution(filename):
    file_dir = os.path.dirname(os.path.realpath('__file__'))
    fl = 'clues/' + filename
    file_path = os.path.join(file_dir, fl)
    fp = open(file_path + ".clues", "r")
    first = fp.readline().strip().split(" ")
    colors = fp.readline().strip().split(" ")
    kind = first[0]
    number_colors = len(colors) - 1
    if kind == "rect":
        number_rows = int(first[1])
        number_columns = int(first[2])
        solution = process_solution(filename, number_columns * number_rows * number_colors)
        var_map = var_map_rect(number_rows, number_columns, len(colors) - 1)
        draw_rectangle(filename, var_map, colors, solution, number_rows, number_columns)
    else:
        size = int(first[1])
        solution = process_solution(filename, number_hex_vars(size, number_colors))
        var_map = var_map_hex(size, len(colors) - 1)
        draw_hexagon(filename, size, var_map, colors, solution)
    fp.close()
    return


def number_hex_vars(size, number_colors):
    result = 0
    for i in range(size, 2 * size):
        if i == 2 * size - 1:
            result = result + i
        else:
            result = result + 2 * i
    return result * number_colors


def process_rect(tup, filename):
    number_rows = int(tup[0][1])
    number_columns = int(tup[0][2])
    number_colors = len(tup[1]) - 1
    row_clues = tup[2]
    column_clues = tup[3]
    #cnfs = []
    clause_counter = 0
    var_counter = number_rows * number_columns * number_colors
    aux_counter = var_counter
    var_map = var_map_rect(number_rows, number_columns, number_colors)
    counter = 1
    # make_masks_rect(row_clues, column_clues, number_rows, number_columns, number_colors)
    for row in row_clues:
        print("Processing row number: " + str(counter))
        print("calculating possibilities ...")
        if filename == "ornament":
            masks = to_mask_big_problem("", row, number_columns, number_columns, number_colors, None, 0)
        else:
            masks = to_mask("", row, number_columns, number_columns, number_colors, None, [])
        # print(masks)
        coords = make_coords_rect("row", counter, -1, -1, number_columns)
        #print("calculating dnf ...")
        #dnf = make_dnf(masks, coords, var_map)
        #print("calculating cnf ...")
        #(cnf, cc, aux_counter) = make_cnf(dnf, aux_counter, filename)
        print("processing possibilities ...")
        if filename == "ornament":
            (cc, aux_counter) = process_masks_big(masks, coords, var_map, aux_counter, filename)
        else:
            (cc, aux_counter) = process_masks(masks, coords, var_map, aux_counter, filename)
        clause_counter = clause_counter + cc
        #write_cnf(cnf, filename)
        #cnfs.append(cnf)
        counter = counter + 1
    counter = 1
    for column in column_clues:
        print("Processing column number: " + str(counter))
        print("calculating possibilities ...")
        if filename == "ornament":
            masks = to_mask_big_problem("", column, number_rows, number_rows, number_colors, None, 1)
        else:
            masks = to_mask("", column, number_rows, number_rows, number_colors, None, [])
        coords = make_coords_rect("column", -1, counter, number_rows, -1)
        #print("calculating dnf ...")
        #dnf = make_dnf(masks, coords, var_map)
        #print("calculating cnf ...")
        #(cnf, cc, aux_counter) = make_cnf(dnf, aux_counter, filename)
        print("processing possibilities ...")
        if filename == "ornament":
            (cc, aux_counter) = process_masks_big(masks, coords, var_map, aux_counter, filename)
        else:
            (cc, aux_counter) = process_masks(masks, coords, var_map, aux_counter, filename)
        clause_counter = clause_counter + cc
        #write_cnf(cnf, filename)
        # cnfs.append(cnf)
        counter = counter + 1
    #write_dimac(cnfs, var_counter + aux_counter, clause_counter, filename)
    write_dimac(var_counter + aux_counter, clause_counter, filename)
    return


"""
def make_masks_rect(row_clues, column_clues, number_rows, number_columns, number_colors):
    row_masks = []
    for row in row_clues:
        row_masks.append(to_mask("", row, number_columns, number_columns, number_colors, None, []))
    print(row_masks[0])
    column_masks = []
    for column in column_clues:
        column_masks.append(to_mask("", column, number_rows, number_rows, number_colors, None, []))
    for i in range(0, len(row_clues)):
        qmark = False
        for clue in row_clues[i]:
            if clue[1] == '?':
                qmark = True
        if qmark:
            col_posb = []
            for j in range(0, len(column_clues)):
                col_posb.append(collect_chars(column_masks[j], i, number_colors))
            for mask in row_masks[i]:
                for k in range(0, len(mask)):
                    mask_copy = mask
                    if mask_copy[k] not in col_posb[k]:
                        if mask in row_masks[i]:
                            row_masks[i].remove(mask)
    print(row_masks[0])
    return row_masks, column_masks
"""

"""
def collect_chars(masks, pos, number_colors):
    chars = []
    for mask in masks:
        if len(chars) == number_colors:
            break
        if mask[pos] not in chars:
            chars.append(mask[pos])
    return chars
"""


def process_hex(tup, filename):
    size = int(tup[0][1])
    number_colors = len(tup[1]) - 1
    row_clues = tup[2]
    column_clues = tup[3]
    column2_clues = tup[4]
    # cnfs = []
    clause_counter = 0
    var_counter = number_hex_vars(size, number_colors)
    aux_counter = var_counter
    var_map = var_map_hex(size, number_colors)
    counter = 1
    for row in row_clues:
        print("Processing row number: " + str(counter))
        print("calculating possibilities ...")
        length = (2 * size - 1) - abs(counter - size)
        masks = to_mask("", row, length, length, number_colors, None, [])
        coords = make_coords_hex("row", counter, -1, -1, size)
        #print("calculating dnf ...")
        #dnf = make_dnf(masks, coords, var_map)
        #print("calculating cnf ...")
        print("processing possibilities ...")
        (cc, aux_counter) = process_masks(masks, coords, var_map, aux_counter, filename)
        #(cnf, cc, aux_counter) = make_cnf(dnf, aux_counter, filename)
        clause_counter = clause_counter + cc
        # cnfs.append(cnf)
        #write_cnf(cnf, filename)
        counter = counter + 1
    counter = 1
    for column in column_clues:
        print("Processing column number: " + str(counter))
        print("calculating possibilities ...")
        length = (2 * size - 1) - abs(counter - size)
        masks = to_mask("", column, length, length, number_colors, None, [])
        coords = make_coords_hex("column", -1, counter, -1, size)
        #print("calculating dnf ...")
        #dnf = make_dnf(masks, coords, var_map)
        #print("calculating cnf ...")
        #(cnf, cc, aux_counter) = make_cnf(dnf, aux_counter, filename)
        print("processing possibilities ...")
        (cc, aux_counter) = process_masks(masks, coords, var_map, aux_counter, filename)
        clause_counter = clause_counter + cc
        #write_cnf(cnf, filename)
        # cnfs.append(cnf)
        counter = counter + 1
    counter = 1
    for column2 in column2_clues:
        print("Processing column2 number: " + str(counter))
        print("calculating possibilities ...")
        length = (2 * size - 1) - abs(counter - size)
        masks = to_mask("", column2, length, length, number_colors, None, [])
        coords = make_coords_hex("column2", -1, -1, counter, size)
        #print("calculating dnf ...")
        #dnf = make_dnf(masks, coords, var_map)
        #print("calculating cnf ...")
        #(cnf, cc, aux_counter) = make_cnf(dnf, aux_counter, filename)
        print("processing possibilities ...")
        (cc, aux_counter) = process_masks(masks, coords, var_map, aux_counter, filename)
        clause_counter = clause_counter + cc
        #write_cnf(cnf, filename)
        # cnfs.append(cnf)
        counter = counter + 1
    # write_dimac(cnfs, var_counter + aux_counter, clause_counter, filename)
    write_dimac(var_counter + aux_counter, clause_counter, filename)
    return


def write_cnf(cnf, filename):
    file_dir = os.path.dirname(os.path.realpath('__file__'))
    fl = 'sat_files/' + filename + "_helper" + ".sat"
    file_path = os.path.join(file_dir, fl)
    fp = open(file_path, "a")
    for clause in cnf:
        line = ""
        for v in clause:
            line = line + str(v) + " "
        line = line + "0\n"
        fp.write(line)
    fp.close()
    return


def write_dimac(number_vars, number_clause, filename):
    file_dir = os.path.dirname(os.path.realpath('__file__'))
    fl = 'sat_files/' + filename + ".sat"
    fl_helper = 'sat_files/' + filename + "_helper" + ".sat"
    file_path = os.path.join(file_dir, fl)
    file_path_helper = os.path.join(file_dir, fl_helper)
    fp = open(file_path, "w")
    fp_helper = open(file_path_helper, "r")
    fp.write("p cnf " + str(number_vars) + " " + str(number_clause) + "\n")
    """
    counter = 0
    for cnf in cnfs:
        for clause in cnf:
            line = ""
            for v in clause:
                line = line + str(v) + " "
            line = line + "0\n"
            if counter % 100 == 0:
                print("writing line: " + str(counter))
            fp.write(line)
            counter = counter + 1
    """
    counter = 0
    line = fp_helper.readline()
    while line:
        #if counter % 100000 == 0:
            #print("writing line: " + str(counter))
        fp.write(line)
        counter = counter + 1
        line = fp_helper.readline()

    fp.close()
    fp_helper.close()
    os.remove(file_path_helper)
    return


def solution_to_ban(filename):
    file_dir = os.path.dirname(os.path.realpath('__file__'))
    fl = 'clues/' + filename
    file_path = os.path.join(file_dir, fl)
    fp = open(file_path + ".clues", "r")
    first = fp.readline().strip().split(" ")
    colors = fp.readline().strip().split(" ")
    kind = first[0]
    number_colors = len(colors) - 1
    nvars = 0
    if kind == "rect":
        number_rows = int(first[1])
        number_columns = int(first[2])
        nvars = number_columns * number_rows * number_colors
        solution = process_solution(filename, number_columns * number_rows * number_colors)
    else:
        size = int(first[1])
        nvars = number_hex_vars(size, number_colors)
        solution = process_solution(filename, number_hex_vars(size, number_colors))
    fp.close()
    print(nvars)
    f = open("ban.txt", "a")
    for s in solution:
        f.write(str(-int(s)) + " ")
    f.write("0\n")
    f.close()
    return


def main():

    print(datetime.datetime.now())

    args = sys.argv[1:]
    filename = args[0]
    mode = args[1]

    if mode == "sat":
        tup = process_specification(filename)
        form = tup[0][0]
        if form == "rect":
            process_rect(tup, filename)
        else:
            process_hex(tup, filename)
    elif mode == "draw":
        draw_solution(filename)
    elif mode == "ban":
        solution_to_ban(filename)
    else:
        print("please enter a valid mode")
        args = sys.argv[1:]


    masks = to_mask("", ["1a", "1?", "1a"], 7, 7, 2, None, [])
    print(len(masks))

    print(datetime.datetime.now())

    # tup = process_specification(filename)
    # process_rect(tup, filename)

    # masks = to_mask("", ["1?","1?","1a", "2b", "1a"], 17, 17, 4, None, [])
    # print(masks)
    # print(len(masks[6]))
    # coords = make_coords_hex("column", -1, 4, -1, 3)
    # print(coords)
    # dnf = [[-1, 2, 3], [1, -2, 3], [1, 2, -3]]
    # dnf2 = [[1,2,3]]
    # !!!! Tsetin ist falsch
    # cnf = Tseitin.Tseitin(dnf)
    # print(cnf)
    # my_cnf = my_tseitin(dnf, 3)
    # print(my_cnf)
    # my_cnf2 = my_tseitin(dnf2, 3)
    # print(my_cnf2)
    # cnf2 = make_cnf(dnf, 1001)
    # print(cnf2)
    # dnf2 = [[1,2,3]]
    # cnf2 = Tseitin.Tseitin(dnf2)
    # print(cnf2)

    # tup = process_specification("rect_black_white" + ".clues")
    # process_rect(tup, "rect_black_white")
    # tup = process_specification("arrow" + ".clues")
    # process_rect(tup, "arrow")
    # draw_solution("rect_black_white")

    # a, b, c = symbols('a, b, c')
    # form = (a & b & ~c) | (~a & b & c) | (~a & b & c)
    # cnf2 = to_cnf(form)
    # print(cnf2)
    # form2 = (a & b & c)
    # cnf2 = to_cnf(form2)
    # print(cnf2)

    # draw_rectangle()
    # draw_hexagon()
    """
    var_map = var_map_hex(3, 2)
    print(var_map)
    coords = make_coords_hex("row", 2, -1, -1, 3)
    print(coords)
    coords2 = make_coords_hex("column", -1, 3, -1, 3)
    print(coords2)
    """

    # test = process_specification("hex_powerbomb" + ".clues")
    # process_hex(test, "hex_powerbomb")
    # draw_solution("hex_powerbomb")


if __name__ == "__main__":
    main()
