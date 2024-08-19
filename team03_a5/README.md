# Repository for team03 (zbMath publication data)
1. Download blazegraph and the *.bz2 dataset and handler.py
2. Open a terminal thar can run python
3. Navigate to the folder of handler.py
4. Parse the dataset by entering "py handler.py parse <dataset>" into the terminal,
   where <dataset> is the name of .bz2 dataset.
   The triples files triples1.ttl to triplesX.ttl will be created.
5. start blazegraph
6. load the triple files into blazegraph by entering "py handler.py load <startIndex> <endIndex> into the terminal,
   where <startIndex> is the number of the first triple file you want to load
   and <endIndex> is the number of the last triple file.
7. To solve a problem file enter "py handler.py solve all" or "py handler.py solve <start> <end>" into the terminal.
   The solution file will be created named "solution.xml".
   Either all problems will be solved, or the problems from <start> to <end>

The solution files are called solution_mini.xml (mini dataset) and solution.xml
The generated RDF for the mini dataset is called triples_mini.ttl
