# Repository for team03 (Picture puzzles)
(a) how to solve a new puzzle:
1. Download the file puzzles.py
2. Create or download the folders: clues, sat_files, sat_solutions, svgs
3. Put the clue file into the clues folder 
4. Open a terminal thar can run python
5. Navigate to the folder of puzzles.py
6. Enter "py puzzles.py <clueFilename> sat" into the terminal, 
   for example "py puzzles.py uni sat"
7. After the programm has finished the new sat file is in the folder sat_files
8. Use a Sat-Solver to solve the sat file (i used the prebuilt kissat solver available at https://github.com/deiruch/kissat/releases/.
   in the git repository there is also a shell command for this solver, but the path to the solver has to be changed for your filesystem)
   (the solution must contain the line "s SATISFIABLE\n" before the solution 
   and the lines with the variables must have 2 random chars beforehand.
   That's the format the kissat solver produced for me.
   If you want to use a other solver with a different format you have to change the method "process_solution")
9. Put the solution of the sat file into the folder sat_solutions, the solution must have the same name as the coresponding clue file
10. To draw the solution enter "py puzzle.py <clueFilename> draw" into the terminal,
    for example "py puzzles.py uni draw"
11. After the programm has finished the new SVG is in the folder svgs
12. To check if the puzzle has another solution enter "py puzzles.py <clueFilename> ban" into the terminal
13. A new file "ban.txt" will be created, open the sat file, increment the clue counter by 1 and add the content of "ban.txt" 
    to the end of the sat file. Delete "ban.txt" afterwards.
14. Use the Sat-Solver for the modified sat file.
15. If there is still a solution, use step 9 to 11 to generate the new svg 
    (you have to rename, delete or replace the old solution and svg)

(b) Uniqueness:
1. ai 	     unique
2. apple     unique
3. arrow     unique
4. circle    unique
5. document  not unique
6. leaf      unique
7. math      unique
8. ornament  unique 
9. pattern   unique
10. pencil     unique
11. portrait   unique
12. puzzle     unique
13. shapes     not unique
14. snow       unique
15. sternhalma unique
16. symmetric  unique
17. trees      not unique
18. triangle   unique
19. uni        unique