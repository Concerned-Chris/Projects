"""
    To use this implementation, you simply have to implement `get_action` such that it returns a legal action.
    You can then let your agent compete on the server by calling
        python3 client_simple.py path/to/your/config.json
"""
import collections
import copy
import itertools
import json
import logging
import math

import requests

import time
import func_timeout

#TODO: rhombus has different coordinates !!!
rhombus = [
    [0, 0],
    [1, 0], [0, 1], [-1, 1], [-1, 0], [0, -1], [1, -1],
    [2, 0], [1, 1], [0, 2], [-1, 2], [-2, 2], [-2, 1], [-2, 0], [-1, -1], [0, -2], [1, -2], [2, -2], [2, -1],
    [3, 0], [2, 1], [1, 2], [0, 3], [-1, 3], [-2, 3], [-3, 3], [-3, 2], [-3, 1], [-3, 0], [-2, -1], [-1, -2], [0, -3],
    [1, -3], [2, -3], [3, -3], [3, -2], [3, -1],
    [-3, 6], [-3, 5], [-2, 5], [-3, 4], [-2, 4], [-1, 4],
    [3, -6], [2, -5], [3, -5], [1, -4], [2, -4], [3, -4]
]
star = [[0, 0],
        [1, 0], [0, 1], [-1, 1], [-1, 0], [0, -1], [1, -1],
        [2, 0], [1, 1], [0, 2], [-1, 2], [-2, 2], [-2, 1], [-2, 0], [-1, -1], [0, -2], [1, -2], [2, -2], [2, -1],
        [3, 0], [2, 1], [1, 2], [0, 3], [-1, 3], [-2, 3], [-3, 3], [-3, 2], [-3, 1], [-3, 0], [-2, -1], [-1, -2],
        [0, -3], [1, -3], [2, -3], [3, -3], [3, -2], [3, -1],
        [3, 3], [2, 3], [3, 2], [1, 3], [2, 2], [3, 1],
        [-3, 6], [-3, 5], [-2, 5], [-3, 4], [-2, 4], [-1, 4],
        [-6, 3], [-5, 2], [-5, 3], [-4, 1], [-4, 2], [-4, 3],
        [-3, -3], [-2, -3], [-3, -2], [-1, -3], [-2, -2], [-3, -1],
        [3, -6], [2, -5], [3, -5], [1, -4], [2, -4], [3, -4],
        [6, -3], [5, -2], [5, -3], [4, -1], [4, -2], [4, -3]]

global distance_a
global distance_b2
global distance_b3
global distance_c

coords = {
    (3, -6): (10, 0),
    (2, -5): (9.5, 1), (3, -5): (10.5, 1),
    (1, -4): (9, 2), (2, -4): (10, 2), (3, -4): (10, 3),
    (-3, -3): (5.5, 3), (-2, -3): (6.5, 3), (-1, -3): (7.5, 3), (0, -3): (8.5, 3), (1, -3): (9.5, 3), (2, -3): (10.5, 3), (3, -3): (11.5, 3), (4, -3): (12.5, 3),
    (5, -3): (13.5, 3), (6, -3): (14.5, 3),
    (-3, -2): (6, 4), (-2, -2): (7, 4), (-1, -2): (8, 4), (0, -2): (9, 4), (1, -2): (10, 4), (2, -2): (11, 4), (3, -2): (12, 4), (4, -2): (13, 4),
    (5, -2): (14, 4),
    (-3, -1): (6.5, 5), (-2, -1): (7.5, 5), (-1, -1): (8.5, 5), (0, -1): (9.5, 5), (1, -1): (10.5, 5), (2, -1): (11.5, 5), (3, -1): (12.5, 5), (4, -1): (13.5, 5),
    (-3, 0): (7, 6), (-2, 0): (8, 6), (-1, 0): (9, 6), (0, 0): (10, 6), (1, 0): (11, 6), (2, 0): (12, 6), (3, 0): (13, 6),
    (-4, 1): (6.5, 7), (-3, 1): (7.5, 7), (-2, 1): (8.5, 7), (-1, 1): (9.5, 7), (0, 1): (10.5, 7), (1, 1): (11.5, 7), (2, 1): (12.5, 7), (3, 1): (13.5, 7),
    (-5, 2): (6, 8), (-4, 2): (7, 8), (-3, 2): (8, 8), (-2, 2): (9, 8), (-1, 2): (10, 8), (0, 2): (11, 8), (1, 2): (12, 8), (2, 2): (13, 8), (3, 2): (14, 8),
    (-6, 3): (5.5, 9), (-5, 3): (6.5, 9), (-4, 3): (7.5, 9), (-3, 3): (8.5, 9), (-2, 3): (9.5, 9), (-1, 3): (10.5, 9), (0, 3): (11.5, 9), (1, 3): (12.5, 9), (2, 3): (13.5, 9),
    (3, 3): (14.5, 9),
    (-3, 4): (9, 10), (-2, 4): (10, 10), (-1, 4): (11, 10),
    (-3, 5): (9.5, 11), (-2, 5): (10.5, 11),
    (-3, 6): (10, 12)
}

global distances

board = None
numberPlayers = None
homesDict = {}
depthLimit = 1
firstTime = False
best_move = None
global a

class Move:
    def __init__(self, spaces, rule, player1, player2):
        self.spaces = spaces
        self.start = spaces[0]
        self.end = spaces[-1]
        self.rule = rule
        self.player1 = player1
        self.player2 = player2
        self.score = -math.inf

def execute_move(move, playersDict):
    playersDict.get(move.player1)[playersDict.get(move.player1).index(move.start)] = move.end
    if move.rule == "swap" or move.rule == "swap hop":
        playersDict.get(move.player2)[playersDict.get(move.player2).index(move.end)] = move.start
    if move.rule == "swap hop chain":
        if move.end in playersDict.get(move.player2):
            playersDict.get(move.player2)[playersDict.get(move.player2).index(move.end)] = move.spaces[-2]
    return playersDict


def revert_move(move, playersDict):
    playersDict.get(move.player1)[playersDict.get(move.player1).index(move.end)] = move.start
    if move.rule == "swap" or move.rule == "swap hop":
        playersDict.get(move.player2)[playersDict.get(move.player2).index(move.start)] = move.end
    if move.rule == "swap hop chain":
        if move.end in playersDict.get(move.player2):
            playersDict.get(move.player2)[playersDict.get(move.player2).index(move.spaces[-2])] = move.end
    return playersDict


def get_adj_spaces(position):
    adj_spaces = []
    if [position[0], position[1] - 1] in board:
        adj_spaces.append(([position[0], position[1] - 1], "left down"))
    if [position[0] + 1, position[1] - 1] in board:
        adj_spaces.append(([position[0] + 1, position[1] - 1], "right down"))
    if [position[0] - 1, position[1]] in board:
        adj_spaces.append(([position[0] - 1, position[1]], "left"))
    if [position[0] + 1, position[1]] in board:
        adj_spaces.append(([position[0] + 1, position[1]], "right"))
    if [position[0] - 1, position[1] + 1] in board:
        adj_spaces.append(([position[0] - 1, position[1] + 1], "left up"))
    if [position[0], position[1] + 1] in board:
        adj_spaces.append(([position[0], position[1] + 1], "right up"))
    return adj_spaces

def get_all_moves(player, playersDict):
    moves = []
    occupied = [[0, 0]]
    occupied = occupied + playersDict.get("A") + playersDict.get("B")
    if numberPlayers == 3:
        occupied = occupied + playersDict.get("C")
    for peg in playersDict.get(player):
        adj_spaces = get_adj_spaces(peg)
        hops = []
        for tup in adj_spaces:
            space = tup[0]
            direction = tup[1]
            if space not in occupied:
                move = Move([peg, space], "simple move", player, "")
                moves.append(move)
            if space in occupied and space not in playersDict.get(player) and space in homesDict.get(player):
                if space in playersDict.get("A"):
                    player2 = "A"
                elif space in playersDict.get("B"):
                    player2 = "B"
                else:
                    player2 = "C"
                move = Move([peg, space], "swap", player, player2)
                moves.append(move)
            if space in occupied:
                target = calc_hop_target(peg, direction)
                if target in board and target not in occupied:
                    move = Move([peg, target], "simple hop", player, "")
                    moves.append(move)
                    hops.append(move)
                if target in board and target in occupied and target not in playersDict.get(player) and target in homesDict.get(player):
                    if target in playersDict.get("A"):
                        player2 = "A"
                    elif target in playersDict.get("B"):
                        player2 = "B"
                    else:
                        player2 = "C"
                    move = Move([peg, target], "swap hop", player, player2)
                    moves.append(move)
        hop_chains(player, peg, hops, occupied, moves, playersDict)
    #print(occupied)
    return moves

def hop_chains(player, peg, hops, occupied, moves, playersDict):
    for hop in hops:
        visited = [peg, hop.end]
        hop_chain(player, hop.end, occupied, moves, visited, playersDict)

def hop_chain(player, current_position, occupied, moves, visited, playersDict):
    adj_spaces = get_adj_spaces(current_position)
    for tup in adj_spaces:
        space = tup[0]
        direction = tup[1]
        if space in occupied:
            target = calc_hop_target(current_position, direction)
            if target in visited:
                continue
            if target in board and target not in occupied:
                visited.append(target)
                move = Move(visited.copy(), "hop chain", player, None)
                moves.append(move)
                target_arg = target.copy()
                hop_chain(player, target_arg, occupied, moves, visited, playersDict)
                visited.remove(target)
            if target in board and target in occupied and target not in playersDict.get(player) and target in homesDict.get(player):
                if target in playersDict.get("A"):
                    player2 = "A"
                elif target in playersDict.get("B"):
                    player2 = "B"
                else:
                    player2 = "C"
                visited.append(target)
                move = Move(visited.copy(), "swap chain hop", player, player2)
                moves.append(move)
                visited.remove(target)


def miniMax(player, depth, alpha, beta, playersDict):
    moves = get_all_moves(player, playersDict)
    if depth == 0 or not moves or has_won(player, playersDict) or has_lost(player, playersDict):
        return player_score(player, playersDict), None
    maxValue = alpha
    best_move = None
    while not moves == []:
        move = get_best_move(player, moves, playersDict)
        moves.remove(move)
        playersDict = execute_move(move, playersDict)
        value = -miniMax(nextPlayer(player), depth-1, -beta, -maxValue, playersDict)[0]
        playersDict = revert_move(move, playersDict)
        if value > maxValue:
            maxValue = value
            if depth == depthLimit:
                best_move = move
            if maxValue >= beta:
                break
    return maxValue, best_move

#TODO: if score sort same use move that changest furthest away peg higher
def sort_moves(moves, p_dir):
    for move in moves:
        move_score(move.player1, move, p_dir)

    moves.sort(reverse=True, key=lambda x: x.score)

    #cut negativ moves, if all are negativ return them
    moves_pos = []
    for move in moves:
        if move.score >= 0:
            moves_pos.append(move)
    if moves_pos:
        return moves_pos
    return moves

def brs(alpha, beta, depth, init_depth, player, p_dir):
    #global a
    #print("depth:" + str(depth))
    bm = None
    if player == "A":
        moves = get_all_moves("A", p_dir)
        moves = sort_moves(moves, p_dir)
        player = "B"
        if depth <= 0 or not moves or has_won("A", p_dir) or has_lost("A", p_dir):
            return [board_score("A", p_dir), None]

    else:
        moves = get_all_moves("B", p_dir) + get_all_moves("C", p_dir)
        moves = sort_moves(moves, p_dir)
        player = "A"
        if depth <= 0 or not moves or has_won("B", p_dir) or has_lost("B", p_dir) or has_won("C", p_dir) or has_lost("C", p_dir):
            return [max(board_score("B", p_dir), board_score("C", p_dir)), None]

    #print(len(moves))
    for move in moves:
        p_dir = execute_move(move, p_dir)
        #print("down")
        tup = brs(-beta, -alpha, depth-1, init_depth, player, p_dir)
        #print("up")
        v = -tup[0]

        p_dir = revert_move(move, p_dir)

        if v >= beta:
            return [v, None]
        if v > alpha and depth == init_depth:
            bm = move
            #a = move
        alpha = max(alpha, v)

    return [alpha, bm]

def nextPlayer(player):
    if numberPlayers == 2:
        if player == "A":
            return "B"
        else:
            return "A"
    else:
        if player == "A":
            return "B"
        elif player == "B":
            return "C"
        else:
            return "A"

def calc_hop_target(space, direction):
    match direction:
        case 'left down':
            return [space[0], space[1] - 2]
        case 'right down':
            return [space[0] + 2, space[1] - 2]
        case 'left':
            return [space[0] - 2, space[1]]
        case 'right':
            return [space[0] + 2, space[1]]
        case 'left up':
            return [space[0] - 2, space[1] + 2]
        case 'right up':
            return [space[0], space[1] + 2]

#high proximity is good = low sum_prox
#TODO: handle path over [0,0]
def proximity(player, p_dir):
    sum_prox = 0
    for peg in p_dir.get(player):
        prox = 0
        for peg2 in p_dir.get(player):
            if peg == peg2:
                continue
            prox = prox + step_between_pegs(peg, peg2, 0)
            #prox in [1,12]
        sum_prox = sum_prox + prox
        #sum in [5, 60]
    return sum_prox

def step_between_pegs(peg1, peg2, steps):
    if peg1[1] == peg2[1]:
        return steps + abs(peg1[0] - peg2[0])
    else:
        if peg1[1] < peg2[1]:
            if peg1[0] > peg2[0]:
                v = step_between_pegs([peg1[0] - 1, peg1[1] + 1], peg2, steps+1)
            else:
                v = step_between_pegs([peg1[0], peg1[1] + 1], peg2, steps + 1)
        else:
            if peg1[1] > peg2[1]:
                if peg1[0] >= peg2[0]:
                    v = step_between_pegs([peg1[0], peg1[1] - 1], peg2, steps + 1)
                else:
                    v = step_between_pegs([peg1[0] + 1, peg1[1] - 1], peg2, steps + 1)
    return v

#TODO: hops_between_pegs(peg1, peg2, hops)

def distance_furthest_peg(player, p_dir):
    if player == "A" or player == "C":
        pl = player
    else:
        if numberPlayers == 2:
            pl = "B2"
        else:
            pl = "B3"
    furthest = distances.get(pl).get(tuple(p_dir.get(player)[0]))

    #high score = good
    # => low distance = good
    for peg in p_dir.get(player):
        distance = distances.get(pl).get(tuple(peg))
        if distance >= furthest:
            furthest = distance
    return 12 - furthest

def distance_score(player, playersDict):
    pegs = playersDict.get(player)
    if player == "B":
        if numberPlayers == 2:
            pl = "B2"
        else:
            pl = "B3"
    else:
        pl = player

    score = 0
    for peg in pegs:
        score = score + (12 - distances.get(pl).get(tuple(peg)))
    return score

def move_score(player, move, playersDict):
    score_before = board_score(player, playersDict)
    playersDict = execute_move(move, playersDict)
    score_after = board_score(player, playersDict)
    playersDict = revert_move(move, playersDict)
    move.score = score_after - score_before
    return score_after - score_before

def board_score(player, playersDict):
    if numberPlayers == 2:
        return player_score(player, playersDict) - player_score(nextPlayer(player), playersDict)
    else:
        p1 = player_score(player, playersDict)
        p2 = player_score(nextPlayer(player), playersDict)
        p3 = player_score(nextPlayer(nextPlayer(player)), playersDict)
        return ((p1 - p2) + (p1 - p3)) / 2

def player_score(player, playersDict):
    if has_won(player, playersDict):
        return 100000
    if has_lost(player, playersDict):
        return -100000

    #dist in [0,60], home in [0,6], furthest in [0,12] and prox in [5, 60]
    dist = distance_score(player, playersDict)
    home = pegs_in_home(player, playersDict)
    furthest = distance_furthest_peg(player, playersDict)
    prox = proximity(player, playersDict)

    score = 0.5 * dist + 2 * home + furthest * 1.2 + 0.05 * (60 - prox)
    #score = dist

    return score

#select the move with the highest score
#if there a multiple choose the move which moves the most behind peg
def get_best_move(player, moves, playersDict):
    bm = None
    maxm = -10000000000
    dup_max = False
    triples = []
    for i in range(len(moves)):
        score = move_score(player, moves[i], playersDict)
        if score == maxm:
            dup_max = True
        if score > maxm:
            maxm = score
            dup_max = False
            bm = moves[i]
        triples.append((moves[i], score, i))

    if dup_max:
        bm = []
        for t in triples:
            if t[1] == max:
                bm.append(t[0])
        most_behind = 20
        for move in bm:
            if player == "B":
                if numberPlayers == 2:
                    pl = "B2"
                else:
                    pl = "B3"
            else:
                pl = player
            behind = distances.get(pl).get(tuple(move.start))
            if behind < most_behind:
                most_behind = behind
                bm = move
    return bm

def has_won(player, playersDict):
    return pegs_in_home(player, playersDict) == 6

def has_lost(player, playersDict):
    moves = get_all_moves(player, playersDict)
    if not moves:
        return True
    if numberPlayers == 2:
        if has_won(nextPlayer(player), playersDict):
            return True
    else:
        if has_won(nextPlayer(player), playersDict) and has_won(nextPlayer(nextPlayer(player)), playersDict):
            return True
    return False

def pegs_in_home(player, playersDict):
    count = 0
    for peg in playersDict.get(player):
        if peg in homesDict.get(player):
            count = count + 1
    return count


#https://blog.finxter.com/how-to-limit-the-execution-time-of-a-function-call/
def run_function(f, max_wait, args, default_value):
    try:
        return func_timeout.func_timeout(max_wait, f, args)
    except func_timeout.FunctionTimedOut:
        pass
    return default_value

def get_action(percept):

    print("at start of action")
    print(percept)

    #print("call")
    try1 = run_function(brs, 30, [-math.inf, math.inf, 3, 3, "A", copy.deepcopy(percept)],
                       'timeout')
    if try1 == 'timeout':
        try2 = run_function(brs, 30, [-math.inf, math.inf, 2, 2, "A", copy.deepcopy(percept)],
                       'timeout')
        if try2 == 'timeout':
            finaltry = brs(-math.inf, math.inf, 1, 1, "A", copy.deepcopy(percept))
            bm = copy.deepcopy(finaltry[1])
        else:
            bm = copy.deepcopy(try2[1])
    else:
        bm = copy.deepcopy(try1[1])

    #print("call finish")
    # best_move = miniMax("A", 3, -100000000000, 10000000000, playersDict)[1]
    if bm is None:
        #    print("bad")
        moves = get_all_moves("A", copy.deepcopy(percept))
        sort_moves(moves, copy.deepcopy(percept))
        bm = copy.deepcopy(moves[0])

    action = copy.deepcopy(bm)

    print(action.spaces)
    #print("should be")
    p = execute_move(action, copy.deepcopy(percept))
    #print(p)

    return copy.deepcopy(action.spaces)

    '''
    # create a 8" x 8" board
    fig = plt.figure(figsize=[8, 8])
    fig.patch.set_facecolor((1, 1, .8))

    ax = fig.add_subplot(111)

    # draw the grid
    for x in range(19):
        ax.plot([x, x], [0, 18], 'k')
    for y in range(19):
        ax.plot([0, 18], [y, y], 'k')

    # scale the axis area to fill the whole figure
    ax.set_position([0, 0, 1, 1])

    # get rid of axes and everything (the figure background will show through)
    ax.set_axis_off()

    # scale the plot area conveniently (the board is in 0,0..18,18)
    ax.set_xlim(-1, 19)
    ax.set_ylim(-1, 19)

    pegs_a = playersDict.get("A")
    #s, = ax.plot(10, 10, 'o', markersize=30, markeredgecolor=(0, 0, 0), markerfacecolor='w', markeredgewidth=2)
    for peg in playersDict.get("A"):
        x = coords.get(tuple(peg))[0]
        y = coords.get(tuple(peg))[1]
        s, = ax.plot(x, y, 'o', markersize=30, markeredgecolor=(0, 0, 0), markerfacecolor='r', markeredgewidth=2)
    for peg in playersDict.get("B"):
        x = coords.get(tuple(peg))[0]
        y = coords.get(tuple(peg))[1]
        s, = ax.plot(x, y, 'o', markersize=30, markeredgecolor=(0, 0, 0), markerfacecolor='b', markeredgewidth=2)
    for peg in playersDict.get("C"):
        x = coords.get(tuple(peg))[0]
        y = coords.get(tuple(peg))[1]
        s, = ax.plot(x, y, 'o', markersize=30, markeredgecolor=(0, 0, 0), markerfacecolor='g', markeredgewidth=2)
    s, = ax.plot(10, 6, 'o', markersize=30, markeredgecolor=(0, 0, 0), markerfacecolor='k', markeredgewidth=2)
    plt.show()
    '''
    #global a
    #global depthLimit
    #depthLimit = 1
    #bm = None
    '''
    while 1 == 1:
        print(depthLimit)
        depthCopy = depthLimit
        if numberPlayers == 2:
            best_move_try = run_function(miniMax, 10, ["A", depthCopy, -100000000000, 10000000000, copy.deepcopy(percept)], 'timeout')[1]
        else:
            tup = run_function(brs, 20, [-100000000000, 10000000000, depthCopy, depthCopy, "A", copy.deepcopy(percept)],
                                         'timeout')
    '''
    #print(a.spaces)
    #print(percept)
    '''
    mv = get_all_moves("A", playersDict.copy())
    found = False

    for m in mv:
        if a.spaces == m.spaces:
            found = True

        if not found:
            #print("illegal move")
            break
        '''
    '''
            best_move_try = copy.deepcopy(tup[1])

        if tup == 'timeout' or depthLimit > 10:
            #print("Timeout at depthLimit: " + str(depthLimit))
            #print(best_move_try.spaces)
            #print(bm)
            break
        else:
            #print(best_move_try.spaces)
            bm = best_move_try
            #print(best_move_try.spaces)
            depthLimit = depthLimit + 1
            #print(best_move.spaces)
    '''

    '''
    if best_move is None:
        print("Hä?")
        moves = get_all_moves("A", playersDict.copy())
        scores = []
        for move in moves:
            scores.append(move_score("A", move, playersDict.copy()))
        y = 3
    '''


    #print("after calc")
    #print(percept)


    '''
    moves = get_all_moves("A", playersDict.copy())

    #if best_move not in moves:
        #print("Aha!")

    occupied = [[0, 0]]
    occupied = occupied + playersDict.get("A") + playersDict.get("B")
    if numberPlayers == 3:
        occupied = occupied + playersDict.get("C")
    print(occupied)
    if best_move.end in occupied:
        if best_move.end in board and best_move.end in occupied and best_move.end not in playersDict.get("A") and best_move.end in homesDict.get(
                "A"):
            print("he")

        print("Ha?")
    '''


def run(config_file, action_function, single_request=False):
    logger = logging.getLogger(__name__)

    with open(config_file, 'r') as fp:
        config = json.load(fp)

    actions = []
    for request_number in itertools.count():
        logger.info(f'Iteration {request_number} (sending {len(actions)} actions)')
        # send request
        response = requests.put(f'{config["url"]}/act/{config["env"]}', json={
            'agent': config['agent'],
            'pwd': config['pwd'],
            'actions': actions,
            'single_request': single_request,
        })
        if response.status_code == 200:
            response_json = response.json()
            #print("response")
            #print(response_json)
            for error in response_json['errors']:
                logger.error(f'Error message from server: {error}')
            for message in response_json['messages']:
                logger.info(f'Message from server: {message}')

            action_requests = response_json['action-requests']
            if not action_requests:
                logger.info('The server has no new action requests - waiting for 1 second.')
                time.sleep(1)  # wait a moment to avoid overloading the server and then try again
            # get actions for next request
            actions = []
            for action_request in action_requests:
                #print("server")
                #print(action_request)
                actions.append({'run': action_request['run'], 'action': action_function(action_request['percept'])})
                #print(actions)
        elif response.status_code == 503:
            logger.warning('Server is busy - retrying in 3 seconds')
            time.sleep(3)  # server is busy - wait a moment and then try again
        else:
            # other errors (e.g. authentication problems) do not benefit from a retry
            logger.error(f'Status code {response.status_code}. Stopping.')
            break

def test():
    global numberPlayers
    numberPlayers = 2
    playersDict = {
        "A": [[3, -6], [-2, 5], [1, -3], [3, -5], [2, -5], [3, -4]],
        #"A": [[-3,6],[-3,5],[-2,5],[-3,4],[-2,4],[-1,4]],
        #"A": [[-3,2],[-1,2],[1,-4],[2,-5],[3,-6],[3,-5]],
        #"B": [[-3,0],[-2,0],[-1,0],[1,0],[2,0],[3,0]]
        #"B": [[1,-4],[2,-4],[3,-4],[2,-5],[3,-5],[3,-6]]
        "B": [[-2, 4], [0, -1], [-1, 4], [-1, 1], [-3, 4], [1, -1]],
    }
    global board
    board = star
    global homesDict
    homesDict = {"A": [[3, -6], [2, -5], [3, -5], [1, -4], [2, -4], [3, -4]],
                 "B": [[-3, 6], [-3, 5], [-2, 5], [-3, 4], [-2, 4], [-1, 4]]}

    moves = get_all_moves("A", playersDict.copy())
    e = 4
    #count = 0
    #while 1 == 1:
    '''
    # create a 8" x 8" board
    fig = plt.figure(figsize=[8, 8])
    fig.patch.set_facecolor((1, 1, .8))

    ax = fig.add_subplot(111)

    # draw the grid
    for x in range(19):
        ax.plot([x, x], [0, 18], 'k')
    for y in range(19):
        ax.plot([0, 18], [y, y], 'k')

    # scale the axis area to fill the whole figure
    ax.set_position([0, 0, 1, 1])

    # get rid of axes and everything (the figure background will show through)
    ax.set_axis_off()

    # scale the plot area conveniently (the board is in 0,0..18,18)
    ax.set_xlim(-1, 19)
    ax.set_ylim(-1, 19)

    pegs_a = playersDict.get("A")
    # s, = ax.plot(10, 10, 'o', markersize=30, markeredgecolor=(0, 0, 0), markerfacecolor='w', markeredgewidth=2)
    for peg in playersDict.get("A"):
        x = coords.get(tuple(peg))[0]
        y = coords.get(tuple(peg))[1]
        s, = ax.plot(x, y, 'o', markersize=30, markeredgecolor=(0, 0, 0), markerfacecolor='r', markeredgewidth=2)
    s, = ax.plot(10, 6, 'o', markersize=30, markeredgecolor=(0, 0, 0), markerfacecolor='k', markeredgewidth=2)
    for peg in playersDict.get("B"):
        x = coords.get(tuple(peg))[0]
        y = coords.get(tuple(peg))[1]
        s, = ax.plot(x, y, 'o', markersize=30, markeredgecolor=(0, 0, 0), markerfacecolor='b', markeredgewidth=2)
    plt.show()
    '''

    '''
    print("A turn")

    #moves = get_all_moves("A", playersDict.copy())
    #b = get_best_move("A", moves, playersDict.copy())
    #ms = []
    #for move in moves:
    #    score = move_score("A", move, playersDict.copy())
    #    ms.append((move, score))
    tup = miniMax("A", 3, -10000, 10000, playersDict.copy())
    best_move = tup[1]
    #sc = move_score("A", best_move, playersDict.copy())
    #bef = board_score("A", playersDict.copy())
    #playersDict = execute_move(best_move, playersDict.copy())
    #af = board_score("A", playersDict.copy())
    #playersDict = revert_move(best_move, playersDict.copy())
    if not best_move:
        print("No best move?")
        break
    if has_won("A", playersDict.copy()):
        print("A has won")
        break
    if has_won("B", playersDict.copy()):
        print("B has won")
        break

    if count > 150:
        break
    playersDict = execute_move(best_move, playersDict.copy())
    print("B turn")
    tup = miniMax("B", 3, -10000, 10000, playersDict.copy())
    best_moveb = tup[1]
    playersDict = execute_move(best_moveb, playersDict.copy())
    count = count + 1
    print(count)
    '''
    s = 3

def init_distances():
    global distance_a
    global distance_b2
    global distance_b3
    global distance_c

    distance_a = {}
    distance_b2 = {}
    distance_b3 = {}
    distance_c = {}
    for pos in board:
        distance_a.update({tuple(pos): step_between_pegs(pos, [-3, 6], 0)})
        distance_b2.update({tuple(pos): step_between_pegs(pos, [3, -6], 0)})
        distance_b3.update({tuple(pos): step_between_pegs(pos, [-3, -3], 0)})
        distance_c.update({tuple(pos): step_between_pegs(pos, [6, -3], 0)})

    global distances
    distances = {
        "A": distance_a,
        "B2": distance_b2,
        "B3": distance_b3,
        "C": distance_c
    }

def test2(p_dir):

    global board
    global numberPlayers
    global homesDict

    board = star
    numberPlayers = 3
    homesDict = {"A": [[-3, 6], [-3, 5], [-2, 5], [-3, 4], [-2, 4], [-1, 4]],
                 "B": [[-3, -3], [-3, -2], [-2, -3], [-3, -1], [-2, -2], [-1, -3]],
                 "C": [[6, -3], [5, -3], [5, -2], [4, -3], [4, -2], [4, -1]]}

    #global a
    global depthLimit
    depthLimit = 1
    bm = None

    tup = brs(-100000000000, 10000000000, 3, 3, "A", copy.deepcopy(p_dir))
    bm = tup[1]

    mv = get_all_moves("A", p_dir)
    sort_moves(mv, p_dir)
    '''
    while 1 == 1:
        # print(depthLimit)
        depthCopy = depthLimit

        tup = run_function(brs, 5, [-100000000000, 10000000000, depthCopy, depthCopy, "A", copy.deepcopy(p_dir)],
                             'timeout')

        #mv = get_all_moves("A", copy.deepcopy(p_dir))
        #found = False

        #for m in mv:
            #if a.spaces == m.spaces:
                #found = True

            #if not found:
                # print("illegal move")
                #break

        best_move_try = tup[1]

        if best_move_try == 'timeout' or depthLimit > 10:
            break
        else:
            # print(best_move_try.spaces)
            bm = best_move_try
            depthLimit = depthLimit + 1
    '''

    if has_won("A", p_dir):
        print("Won!")
        return
    else:
        print(p_dir)
        print(bm.spaces)
        print(bm.score)

        pdir = execute_move(bm, p_dir)
        test2(pdir)
    return



def test_execute_move():
    global board
    global numberPlayers
    global homesDict

    board = star
    numberPlayers = 3
    homesDict = {"A": [[-3, 6], [-3, 5], [-2, 5], [-3, 4], [-2, 4], [-1, 4]],
                 "B": [[-3, -3], [-3, -2], [-2, -3], [-3, -1], [-2, -2], [-1, -3]],
                 "C": [[6, -3], [5, -3], [5, -2], [4, -3], [4, -2], [4, -1]]}

    p_dir = {"A": [[3, -6], [2,-5], [3,-5], [1,-4], [2,-4], [3,-4]],
             "B": [[1,3], [2,2], [3,1], [2,3], [3,2], [3,3]],
             "C": [[-6,3], [-5,3], [-5,2], [-4,1], [-4,2], [-4,3]]}

    m = Move([[2,-5], [2, -3]], "simple hop", "A", None)

    p_dir_after = {"A": [[3, -6], [2, -3], [3, -5], [1, -4], [2, -4], [3, -4]],
             "B": [[1, 3], [2, 2], [3, 1], [2, 3], [3, 2], [3, 3]],
             "C": [[-6, 3], [-5, 3], [-5, 2], [-4, 1], [-4, 2], [-4, 3]]}

    p_dir = execute_move(m, p_dir)
    first_set = set(map(tuple, p_dir.get("A")))
    secnd_set = set(map(tuple, p_dir_after.get("A")))

    assert first_set == secnd_set, "same"

def test_revert_move():
    global board
    global numberPlayers
    global homesDict

    board = star
    numberPlayers = 3
    homesDict = {"A": [[-3, 6], [-3, 5], [-2, 5], [-3, 4], [-2, 4], [-1, 4]],
                 "B": [[-3, -3], [-3, -2], [-2, -3], [-3, -1], [-2, -2], [-1, -3]],
                 "C": [[6, -3], [5, -3], [5, -2], [4, -3], [4, -2], [4, -1]]}

    p_dir_after = {"A": [[3, -6], [2, -3], [3, -5], [1, -4], [2, -4], [3, -4]],
                   "B": [[1, 3], [2, 2], [3, 1], [2, 3], [3, 2], [3, 3]],
                   "C": [[-6, 3], [-5, 3], [-5, 2], [-4, 1], [-4, 2], [-4, 3]]}

    m = Move([[2, -5], [2, -3]], "simple hop", "A", None)

    p_dir = {"A": [[3, -6], [2, -5], [3, -5], [1, -4], [2, -4], [3, -4]],
             "B": [[1, 3], [2, 2], [3, 1], [2, 3], [3, 2], [3, 3]],
             "C": [[-6, 3], [-5, 3], [-5, 2], [-4, 1], [-4, 2], [-4, 3]]}

    p_dir_after = revert_move(m, p_dir_after)
    first_set = set(map(tuple, p_dir.get("A")))
    secnd_set = set(map(tuple, p_dir_after.get("A")))

    assert first_set == secnd_set, "same"

def test_board_score():
    global board
    global numberPlayers
    global homesDict

    board = star
    numberPlayers = 3
    homesDict = {"A": [[-3, 6], [-3, 5], [-2, 5], [-3, 4], [-2, 4], [-1, 4]],
                 "B": [[-3, -3], [-3, -2], [-2, -3], [-3, -1], [-2, -2], [-1, -3]],
                 "C": [[6, -3], [5, -3], [5, -2], [4, -3], [4, -2], [4, -1]]}

    p_dir = {"A": [[3, -6], [2, -3], [3, -5], [1, -4], [2, -4], [3, -4]],
             "B": [[1, 3], [2, 2], [3, 1], [2, 3], [3, 2], [3, 3]],
             "C": [[-6, 3], [-5, 3], [-5, 2], [-4, 1], [-4, 2], [-4, 3]]}

    score = board_score("A", p_dir)

    assert score == 2.0

def doesnt_finish():
    global board
    global numberPlayers
    global homesDict

    board = star
    numberPlayers = 3
    homesDict = {"A": [[-3, 6], [-3, 5], [-2, 5], [-3, 4], [-2, 4], [-1, 4]],
                 "B": [[-3, -3], [-3, -2], [-2, -3], [-3, -1], [-2, -2], [-1, -3]],
                 "C": [[6, -3], [5, -3], [5, -2], [4, -3], [4, -2], [4, -1]]}

    p_dir = {"A": [[-1, 4], [-3, 6], [-3, 5], [-2, 5], [-2, 3], [-2, 4]],
             "B": [[1, 3], [2, 2], [3, 1], [2, 3], [3, 2], [3, 3]],
             "C": [[-6, 3], [-5, 3], [-5, 2], [-4, 1], [-4, 2], [-4, 3]]}

    moves = get_all_moves("A", p_dir)
    sort_moves(moves, p_dir)

    bm = brs(-1000000, 1000000, 3, 3, "A", p_dir)[1]

    x = 3

if __name__ == '__main__':
    import sys
    #test()
    test_execute_move()
    test_revert_move()

    fileName = sys.argv[1]
    #fileName = "ws2223-2.6-medium-3-player.json"
    if "2.1" in fileName:
        board = rhombus
        numberPlayers = 2
        homesDict = {"A": [[-3,6],[-3,5],[-2,5],[-3,4],[-2,4],[-1,4]],
                     "B": [[3,-6],[2,-5],[3,-5],[1,-4],[2,-4],[3,-4]]}
    elif "2.2" in fileName or "2.3" in fileName or "2.4" in fileName:
        board = star
        numberPlayers = 2
        homesDict = {"A": [[-3, 6], [-3, 5], [-2, 5], [-3, 4], [-2, 4], [-1, 4]],
                     "B": [[3, -6], [2, -5], [3, -5], [1, -4], [2, -4], [3, -4]]}
    else:
        board = star
        numberPlayers = 3
        homesDict = {"A": [[-3,6],[-3,5],[-2,5],[-3,4],[-2,4],[-1,4]],
                     "B": [[-3,-3],[-3,-2],[-2,-3],[-3,-1],[-2,-2],[-1,-3]],
                     "C": [[6,-3],[5,-3],[5,-2],[4,-3],[4,-2],[4,-1]]}

    init_distances()

    #test_board_score()

    p_dir = {"A": [[3, -6], [2, -5], [3, -5], [1, -4], [2, -4], [3, -4]],
             "B": [[1, 3], [2, 2], [3, 1], [2, 3], [3, 2], [3, 3]],
             "C": [[-6, 3], [-5, 3], [-5, 2], [-4, 1], [-4, 2], [-4, 3]]}

    #doesnt_finish()

    #test2(p_dir)

    run(fileName, get_action)


