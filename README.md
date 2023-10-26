# AI-And-Multi-Agent-Systems

## 1) -

## 2.1)

Our solution was 2 moves to the south, 10 moves to the east and 2 moves to the south whereas GraphSearch's solution was 1 move to the south, 1 move to the east, 1 move to the south, 9 moves to the east and 2 moves to the south.

The difference in the solutions is that our solution tried to reach the objective with as litle changes in direction as possible. This implies having a global view of the state of the problem. GraphSearch used BFS, an uninformed search algorithm, to reach the objective which took it in a different route comprised of many changes in direction since it the algorithm is not aware of where the goal is.

## 2.2) -

## 2.3) 

The differences in performance can be caused by the difference in the map size. Large maps tend to have more cells and therefore require more time to be searched. The difference in the number of agents also has an influence on the performance of the search in each level because more agents lead to more states that can be explored.

The number of generated states grows by a factor x when a new agent is added to a specific level because the search space expands as each agent has its own set of actions to take, and the number of actions that can be taken by each agent in the state space increases with each new agent.

### missing table

## 3.1)

BFS is preferable when the solution is likely to be closer to the root of the search tree and the state space is relatively small. The implementation of BFS uses a queue to keep track of the nodes to be explored next and its complexity is O(b^d) where b is the branching factor and d is the depth of the goal state. BFS always finds the optimal solution as long as the path cost grows with the depth of the nodes.

On the other hand DFS is preferable when the solution is likely to be deeper in the search tree and the state space is very large. The implementation of DFS uses a stack to keep track of the nodes to be explored next and its complexity is O(b^m) where b is the branching factor and m is the maximum depth of search tree. 

Contrary to BFS, DFS might not find the optimal solution and is often used to find a solution quickly even if it's not optimal. 

This can be observed in the results:

* DFS is (almost) always quicker than BFS in reaching a solution (except for the level MAPFslidingpuzzle);

* The length of the solution reached though DFS is always equal or greater than that of the solution found using BFS.

| Level  | Strategy | States Generated  | Time/s | Solution length |
| ------------- | ------------- | ------------- | ------------- | ------------- |
| MAPF00 | BFS | 48 | 0.066 | 14 |
| MAPF00 | DFS | 41 | 0.064 | 18 |
| MAPF00 | A* | 48 | 0.071 | 14 |
| MAPF00 | Greedy | 45 | 0.071 | 16 |


| Level  | Strategy | States Generated  | Time/s | Solution length |
| ------------- | ------------- | ------------- | ------------- | ------------- |
| MAPF01 | BFS  | 2'350  | 0.167  | 14 |
| MAPF01 | DFS | 1'270 | 0.140 | 147 |
| MAPF01 | A* | 2'311 | 0.172 | 14 |
| MAPF01 | Greedy | 1'771 | 0.150 | 137 |


| Level  | Strategy | States Generated  | Time/s | Solution length |
| ------------- | ------------- | ------------- | ------------- | ------------- |
| MAPF02 | BFS  | 110'445  | 4.191  | 14 |
| MAPF02 | DFS | 8'218 | 0.186 | 207 |
| MAPF02 | A* | 108'206 | 4.707 | 14 |
| MAPF02 | Greedy | 18'451 | 0.227 | 206 |


| Level  | Strategy | States Generated  | Time/s | Solution length |
| ------------- | ------------- | ------------- | ------------- | ------------- |
| MAPF02C | BFS | 110'540 | 4.165 | 14 |
| MAPF02C | DFS | 86'870 | 0.671 | 3'538 |
| MAPF02C | A* | 106'051 | 4.635 | 14 |
| MAPF02C | Greedy | 5'168 | 0.163 | 44 |


| Level  | Strategy | States Generated  | Time/s | Solution length |
| ------------- | ------------- | ------------- | ------------- | ------------- |
| MAPF03 | BFS | - | timeout | - |
| MAPF03 | DFS | 128'511 | 0.495 | 608 |
| MAPF03 | A* | - | timeout | - |
| MAPF03 | Greedy | 156'727 | 0.575 | 364 |


| Level  | Strategy | States Generated  | Time/s | Solution length |
| ------------- | ------------- | ------------- | ------------- | ------------- |
| MAPF03C | BFS | - | timeout | - |
| MAPF03C | DFS | 4'230'326 | 36.835 | 52'998 |
| MAPF03C | A* | - | timeout | - |
| MAPF03C | Greedy | 129'608 | 1.603 | 55 |


| Level  | Strategy | States Generated  | Time/s | Solution length |
| ------------- | ------------- | ------------- | ------------- | ------------- |
| MAPFslidingpuzzle | BFS | 181'289 | 1.089 | 28 |
| MAPFslidingpuzzle | DFS | 163'454 | 2.451 | 57'558 |
| MAPFslidingpuzzle | A* | 104'391 | 0.846 | 28 |
| MAPFslidingpuzzle | Greedy | 962 | 0.137 | 46 |


| Level  | Strategy | States Generated  | Time/s | Solution length |
| ------------- | ------------- | ------------- | ------------- | ------------- |
| MAPFreorder2 | BFS | 3'603'559 | 167.990 | 38 |
| MAPFreorder2 | DFS | 3'595'945 | 79.646 | 78'862 |
| MAPFreorder2 | A* | 3'600'155 | 165.399 | 40 |
| MAPFreorder2 | Greedy | 1'583'879 | 34.733 | 389 |

## 3.2)

A level where BFS outperforms DFS is a level with a low branching factor where the solution is close to the root of the search tree rather than at a high depth. The low branching factor makes it so there are less states to explore at each depth level and the low depth of the solution makes BFS end its search early.

A maze-like structure is a good example of such a level. The branching factor is limited by the walls surrounding the path to the solution. An elongated dead end can make DFS waste a lot of time expanding states that turn out to lead nowhere.

To increase the difference in performance between BFS and DFS arbitrarily one would simply need to add more dead ends to the level and/or make them longer.

| Level  | Strategy | States Generated  | Time/s | Solution length |
| ------------- | ------------- | ------------- | ------------- | ------------- |
| BFSfriendly | BFS | 16 | 0.059 | 8 |
| BFSfriendly | DFS | 58 | 0.074 | 8 |

## 4.1) -

## 4.2) 

A* and Greedy best-first search are usually faster than BFS and DFS because they provide additional information to guide the search and prioritize nodes to be explored.

In A* the cost of reaching a node is combined with a heuristic that estimates the cost of reaching the goal from the node. The nodes with the lowest combined value of cost and heuristic are explored first allowing A* to prioritize nodes that are more likely to lead to the goal.

Similarly, in greedy best-first search, the heuristic is used to estimate the cost of reaching the goal from a node, and nodes are explored in the order of increasing heuristic value. This allows greedy best-first search to focus on the nodes that are closest to the goal and avoid exploring nodes that are far away.

BFS and DFS do not use any information to guide the search and explore nodes in a fixed order. As a result these algorithms can be much slower than their A* and Greedy best-first search counterparts.

Curiously, the results suggest a pairwise similarity in performance between BFS-A* and DFS-Greedy.

The difference in performance of A* and Greedy over BFS and DFS was not significant until level MAPF03C. In fact, for levels with a relatively low number of generated states the latter outperform the former. This might be due to the fact that A* and Greedy imply overhead computations that don't pay off for easy-to-solve levels.

However, for levels with a high number of generated states A* and Greedy generally perform much better than BFS and DFS.

### missing table

## 4.3) 

### Pseudocode for calculation of heuristic

```
// initialize goal coordinates
for position in state:
    if position is goal:
        add map entry <goal, goal coordinates>

// calculate sum of manhattan distances between agents/boxes and goals
for agent/box in state:
    if agent/box in <goal, goal coordinates> map:
        manhattanDistanceSum += distance(agent/box, goal)
```

The intuition behind this heuristic is that the sum of the distances between each agent and its goal gives an estimate of the number of moves required to reach the goal state. The closer each agent is to its goal the fewer moves are required to reach the goal state.

This heuristic has the strength of being a simple and efficient calculation. However, its weaknesses are that its an oversimplification of the problem. It doesn't take into account box goals nor does it take into account the multiple possible goal positions for an agent, only the last position it came across. This can cause inaccurate estimations of how close the given state is to being a solution which in turn causes the heuristic to be inconsistent, working well for some levels but poorly for others.

### Manhattan Distance to Agent Goals Sum Heuristic

| Level  | Strategy | States Generated  | Time/s | Solution length |
| ------------- | ------------- | ------------- | ------------- | ------------- |
| MAPF00 | A* | 44 | 0.070 | 14 |
| MAPF00 | Greedy | 32 | 0.063 | 18 |


| Level  | Strategy | States Generated  | Time/s | Solution length |
| ------------- | ------------- | ------------- | ------------- | ------------- |
| MAPF01 | A* | 856 | 0.111 | 14 |
| MAPF01 | Greedy | 493 | 0.118 | 24 |


| Level  | Strategy | States Generated  | Time/s | Solution length |
| ------------- | ------------- | ------------- | ------------- | ------------- |
| MAPF02 | A* | 44'792 | 0.572 | 14 |
| MAPF02 | Greedy | 988 | 0.098 | 14 |


| Level  | Strategy | States Generated  | Time/s | Solution length |
| ------------- | ------------- | ------------- | ------------- | ------------- |
| MAPF02C | A* | 2'325 | 0.144 | 20 |
| MAPF02C | Greedy | 987 | 0.125 | 22 |


| Level  | Strategy | States Generated  | Time/s | Solution length |
| ------------- | ------------- | ------------- | ------------- | ------------- |
| MAPF03 | A* | 457'586 | 5.993 | 14 |
| MAPF03 | Greedy | 716'298 | 76.673 | 37 |


| Level  | Strategy | States Generated  | Time/s | Solution length |
| ------------- | ------------- | ------------- | ------------- | ------------- |
| MAPF03C | A* | 4,949 | 0.192 | 18 |
| MAPF03C | Greedy | 3'895 | 0.153 | 21 |


| Level  | Strategy | States Generated  | Time/s | Solution length |
| ------------- | ------------- | ------------- | ------------- | ------------- |
| MAPFslidingpuzzle | A* | 3'190 | 0.170 | 28 |
| MAPFslidingpuzzle | Greedy | 311 | 0.136 | 58 |


| Level  | Strategy | States Generated  | Time/s | Solution length |
| ------------- | ------------- | ------------- | ------------- | ------------- |
| MAPFreorder2 | A* | 1'945'739 | 69.552 | 51 |
| MAPFreorder2 | Greedy | 1'813'567 | 77.822 | 175 |

## 4.4) TODO - video

## 5.1) -

## 5.2)

The primary factor that makes SAD2 harder to solve using BFS compared to SAD1 is the increased branching factor of the search tree in SAD2. 

The branching factor refers to the average number of children for each node in the search tree. In SAD2 there are more boxes and agents to move which leads to a higher branching factor. As a result the search tree grows much more quickly and the number of nodes that need to be explored to find the solution is much larger. 

This makes the search space larger leading to slower performance since BFS explores all nodes in order of increasing depth. SAD1 has a smaller branching factor which makes it easier to solve using BFS.

SAD3 is much harder than SAD2 for the same reasons.

| Level  | Strategy | States Generated  | Time/s | Solution length |
| ------------- | ------------- | ------------- | ------------- | ------------- |
| SAD1 | BFS | 78 | 0.079 | 19 |
| SAD2 | BFS | 459 680 | 2.092 | 19 |
| SAD3 | BFS | - | timeout | - |

## 5.3)

| Level  | Strategy | States Generated  | Time/s | Solution length |
| ------------- | ------------- | ------------- | ------------- | ------------- |
| SAFirefly | BFS | 2 260 942 | 12.285 | 60 |
| SAFirefly | DFS | 4 860 593 | 50.92 | 2 427 580 |
| SACrunch | BFS | - | timeout | - |
| SACrunch | DFS | 1 801 503 | 8.812 | 473 998 |

## 6.1) 

| Level  | Strategy | States Generated  | Time/s | Solution length |
| ------------- | ------------- | ------------- | ------------- | ------------- |
| SAFirefly | A* | 1 387 034 | 16.636 | 60 |
| SAFirefly | Greedy | 6 372 | 0.216 | 288 |
| SACrunch | A* | - | timeout | - |
| SACrunch | Greedy | 17 357 | 0.232 | 249 |

## 6.2)

To improve out heuristic we have a few ideas in mind: 

* Combining the manhattan distance heuristic with the goal count heuristic would allow us to take into account not only the distance to the goals but also the number of goals that are yeat to be fulfilled. This could help us to better evaluate the potential cost of a particular path and to identify the most promising route to reach a solution.

* Excluding the distance of boxes to box goals of goals whose type already fulfills all corresponding goals. This way if, for example, all goals of type 'A' are fulfilled with a box of type 'A' then the distance of boxes of type 'A' to goals of type 'A' should be excluded from the manhattan distance sum, therefore giving a state where 'A' goals are fulfilled a lower heuristic value.

* Considering the distance between agents and boxes would allow us to give lower heuristic values to states in which the agents are close to the boxes they intend to move. By taking into account the proximity between agents and boxes, we can prioritize states where the agents can quickly and easily move the boxes towards their goals.

