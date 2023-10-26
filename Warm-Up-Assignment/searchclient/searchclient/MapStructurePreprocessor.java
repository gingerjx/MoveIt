package searchclient;

import java.util.*;
import java.util.logging.Level;

/*
    Helper class to compute distances and paths, which are persisted in LevelMetadata
 */
public class MapStructurePreprocessor {

    private final int mapRows = LevelMetadata.MAP_ROWS;
    private final int mapCols = LevelMetadata.MAP_COLS;
    private final int mapArea = LevelMetadata.MAP_AREA;
    private int[] branchCells = new int[0];
    private int[][][][] distances;
    private int[][][][] pathsRow;
    private int[][][][] pathsCol;


    public MapStructurePreprocessor() {
        initDistancesAndPaths();

        for (int row=0; row < mapRows; row++) {
            for (int col=0; col < mapCols; col++) {
                if (!StateUtils.cellIsWall(row, col)) {
                    compute(row, col);
                    int numOfWalls = StateUtils.getNumOfWallsAround(row, col);
                    if (numOfWalls < 2) {
                        addBranchCell(row, col);
                    }
                }

            }
        }

        System.gc(); //Force unused memory release
    }

    public int[][][][] getDistances() {
        return distances;
    }

    public int[][][][] getPathsRow() {
        return pathsRow;
    }

    public int[][][][] getPathsCol() {
        return pathsCol;
    }

    public int[] getBranchCells() {
        return branchCells;
    }

    private void initDistancesAndPaths() {
        distances = new int[mapRows][mapCols][mapRows][mapCols];
        pathsRow = new int[mapRows][mapCols][mapRows][mapCols];
        pathsCol = new int[mapRows][mapCols][mapRows][mapCols];
        for (int i=0; i<mapRows; i++) {
            for (int j=0; j<mapCols; j++) {
                for (int k=0; k<mapRows; k++) {
                    Arrays.fill(distances[i][j][k], mapArea);
                    Arrays.fill(pathsRow[i][j][k], -1);
                    Arrays.fill(pathsCol[i][j][k], -1);
                }
            }
        }
    }

    public int[][] getMapSplit(State initialState){
        int[][] mapSplit=new int[mapRows][mapCols];
        int subMap = 1;
        for (int[] row: mapSplit)
            Arrays.fill(row,0); //populate map grid with 0s

        for (int row = 1; row < mapRows - 1; row++) {
            for (int col = 1; col < mapCols - 1; col++) {
                if (!State.WALLS[row][col] && mapSplit[row][col]==0) { //found new cell that is not a wall
                    for (int rowTrg = 1; rowTrg < mapRows - 1; rowTrg++) {
                        for (int colTrg = 1; colTrg < mapCols - 1; colTrg++) {
                            if (distances[row][col][rowTrg][colTrg]!=mapArea){
                                mapSplit[rowTrg][colTrg] = subMap;
                            }
                        }
                    }
                    subMap += 1;
                }
            }
        }
        return mapSplit;
    }

    public State replaceBoxWall(State initialState){
        int[] agentRows = initialState.agentRows;
        int[] agentCols = initialState.agentCols;
        Color[] agentColors = State.AGENT_COLORS;
        char[][] boxes = initialState.boxes; 
        Color[] boxColors = State.BOX_COLORS;
        char[][] goals = State.GOALS;
        int[] agentSplit = new int[10];
        int[][] mapSplit = LevelMetadata.MAPSPLIT;

        for (int a = 0; a <agentRows.length;a++){
            agentSplit[a] = mapSplit[agentRows[a]][agentCols[a]];
        }

        for (int row = 1; row < mapRows - 1; row++) {
            for (int col = 1; col < mapCols - 1; col++) { //check all cells
                int inspectedSplit = mapSplit[row][col]; //subMap number of the cell
                char box = boxes[row][col];
                if ('A'<=box && box <= 'Z'){ //if the character is a box we will check if we should delete
                    boolean[] potentialAgents = StateUtils.agentsForBox(initialState, box); //returns array with a boolean for each agent, stating if same color from box
                    boolean flagDelete = true; //assume we will replace the box with a wall (delete)
                    for (int a = 0; a < potentialAgents.length; a++) {
                        if (potentialAgents[a] && agentSplit[a]==inspectedSplit){
                            flagDelete = false; //if one of the agents from same color is in the same submap - do not delete
                        }
                    }   
                    if (flagDelete){ //delete
                        if (goals[row][col] != boxes[row][col]){ //delete only if it is not a box already in goal
                            boxes[row][col] = boxes[0][0]; //remove from boxes grid
                            State.WALLS[row][col] = true; //add a wall to walls
                        }
                        
                    }
                }
            }
        }
        
        return new State(agentRows, agentCols, agentColors, State.WALLS, boxes, boxColors, goals);
    }

    private void compute(int startRow, int startCol) {
        distances[startRow][startCol][startRow][startCol] = 0;
        int[][] pointDistances = distances[startRow][startCol];
        int[][] pointPathsRow = pathsRow[startRow][startCol];
        int[][] pointPathsCol = pathsCol[startRow][startCol];
        ArrayDeque<Coordinates> queue = new ArrayDeque<>(65536);
        queue.addLast(new Coordinates(startRow, startCol));
        initPaths(new Coordinates(startRow, startCol));

        while (true) {
            if (queue.isEmpty()) {
                break;
            }

            Coordinates coords = queue.pollFirst();

            List<Coordinates> adjCells = getAdjacentCells(coords);
            for (Coordinates adjCoords : adjCells) {
                int row = adjCoords.row;
                int col = adjCoords.col;
                if (!coordsOutOfBounds(adjCoords) && !StateUtils.cellIsWall(row, col) && pointDistances[row][col] == mapArea) {
                    pointDistances[row][col] = getCellDistanceValue(pointDistances, new Coordinates(row, col));
                    if (pointDistances[row][col] > 1) {
                        pointPathsRow[row][col] = pointPathsRow[coords.row][coords.col];
                        pointPathsCol[row][col] = pointPathsCol[coords.row][coords.col];
                    }
                    queue.addLast(adjCoords);
                }
            }
        }
    }

    private void addBranchCell(int row, int col) {
        int len = branchCells.length;
        branchCells = Arrays.copyOf(branchCells, len + 2);
        branchCells[len] = row;
        branchCells[len + 1] = col;
    }

    private void initPaths(Coordinates coords) {
        List<Coordinates> adjCells = getAdjacentCells(coords);
        for (Coordinates adjCoords : adjCells) {
            int row = adjCoords.row;
            int col = adjCoords.col;
            if (!coordsOutOfBounds(adjCoords) && !StateUtils.cellIsWall(row, col)) {
                pathsRow[coords.row][coords.col][row][col] = row;
                pathsCol[coords.row][coords.col][row][col] = col;
            }
        }
    }

    private int getCellDistanceValue(int[][] distances, Coordinates coords) {
        int min = mapArea;
        for (Coordinates adjCoords : getAdjacentCells(coords)) {
            min = Math.min(distances[adjCoords.row][adjCoords.col], min);
        }
        return min + 1;
    }

    private List<Coordinates> getAdjacentCells(Coordinates coords) {
        List<Coordinates> adjCoords = new ArrayList<>();
        for (Action action : List.of(Action.MoveE, Action.MoveS, Action.MoveW, Action.MoveN)) {
            Coordinates newCoords = new Coordinates(coords.row + action.agentRowDelta, coords.col + action.agentColDelta);
            if (!coordsOutOfBounds(newCoords)) {
                adjCoords.add(newCoords);
            }
        }
        return adjCoords;
    }

    private boolean coordsOutOfBounds(Coordinates coords) {
        return coords.row < 0 || coords.row >= mapRows || coords.col < 0 || coords.col >= mapCols;
    }

    public void rankGoals() {
        HashMap<Coordinates, Integer> goalRank = LevelMetadata.goalRank;
        boolean[][] walls = State.WALLS;
        char[][] goals = State.GOALS;

        GoalCluster[] goalClusters = getGoalClusters(goalRank,walls,goals);
        for (GoalCluster cluster: goalClusters){
            if (cluster == null){break;}
            cluster.rankGoal();
        }
    }
    public class GoalCluster {
        ClusterType type;
        /*
        if cluster of adjacent goals:
            for each goal in the cluster, we create an array with 5 pairs of coordinates:
            -the goal coordinates -> x1,x2
            - right / bottom / left / top -> x3,x4/x5,x6/x7,x8/x9,x10
            -for the surroundings, we can have:
                - a wall (represented by -1,-1),
                - a non-wall,non-goal cell (represented by 0,0),
                - a goal (represented by goal coordinates)
        if cluster of deadend:
            for each goal in the cluster, we create an array with 3 elements:
            -the goal coordinates -> x1,x2
            -the position within the deadend -> x3
         */
        int[][] goals;
        int[][] deadend;
        int size;
        int totalGoals;
        public GoalCluster(Coordinates coord, ClusterType type) {
            this.type = type;
            this.totalGoals = LevelMetadata.goalRank.size();
            if (type == ClusterType.ADJACENT){
                this.goals = new int[this.totalGoals][10];
                goals[0][0] = coord.row; goals[0][1] = coord.col;
                List<Coordinates> adjCells = getAdjacentCells(coord);
                int i = 2;
                for (Coordinates cell: adjCells){
                    if (State.WALLS[cell.row][cell.col]){ //adjacent cell is a wall
                        goals[0][i]=-1; goals[0][i+1]=-1;
                        i +=2;
                    } else if (StateUtils.isGoal(cell.row,cell.col)){ //adjacent cell is a goal
                        goals[0][i]=cell.row; goals[0][i+1]=cell.col;
                        i +=2;
                    } else { //adjacent cell is not goal or wall
                        goals[0][i]=0; goals[0][i+1]=0;
                        i +=2;
                    }
                }
                size = 1;
                for (Coordinates cell :adjCells){
                    if (StateUtils.isGoal(cell.row,cell.col) && isNotInCluster(cell)) {
                        this.add(cell);
                    }
                }

            }
        }

        public GoalCluster(Coordinates coord, int[][] deadend, ClusterType type){
            this.type = type;
            this.deadend = deadend;
            this.totalGoals = LevelMetadata.goalRank.size();
            if (type == ClusterType.DEADEND){
                this.goals = new int[this.totalGoals][3];
                goals[0][0] = coord.row; goals[0][1] = coord.col;
                goals[0][2] = 0;
                for (int i=0; i<deadend.length;i++){
                    if(deadend[i][0] == coord.row && deadend[i][1] == coord.col){
                        goals[0][2] = i;
                    };
                }
                size = 1;
            }
            for (int[] xy:deadend){
                if (StateUtils.isGoal(xy[0], xy[1]) && isNotInCluster(new Coordinates(xy[0], xy[1]))){
                    this.add(new Coordinates(xy[0], xy[1]));
                }
            }
        }

        public void add(Coordinates coord){
            goals[size][0] = coord.row; goals[size][1] = coord.col;
            if (this.type == ClusterType.ADJACENT){
                List<Coordinates> adjCells = getAdjacentCells(coord);
                int i = 2;
                for (Coordinates cell: adjCells){
                    if (State.WALLS[cell.row][cell.col]){ //adjacent cell is a wall
                        goals[size][i]=-1; goals[size][i+1]=-1;
                        i +=2;
                    } else if (StateUtils.isGoal(cell.row,cell.col)){ //adjacent cell is a goal
                        goals[size][i]=cell.row; goals[size][i+1]=cell.col;
                        i +=2;
                    } else { //adjacent cell is not goal or wall
                        goals[size][i]=0; goals[size][i+1]=0;
                        i +=2;
                    }
                }
                size +=1;
                for (Coordinates cell :adjCells){
                    if (StateUtils.isGoal(cell.row,cell.col) && isNotInCluster(cell)) {
                        this.add(cell);
                    }
                }
            } else if (this.type == ClusterType.DEADEND){
                goals[size][0] = coord.row; goals[size][1] = coord.col;
                for (int i=0; i<this.deadend.length;i++){
                    if(this.deadend[i][0] == coord.row && this.deadend[i][1] == coord.col){
                        goals[size][2] = i;
                    };
                }
                size += 1;
            }
        }

        public void rankGoal() {
            if (this.type == ClusterType.ADJACENT){rankGoalAdj();}
            else if (this.type == ClusterType.DEADEND) {rankGoalDead();}
        }

        private void rankGoalAdj() {
            int[][] counters = new int[size][3]; //nb of adj goals + walls - nb of adj walls - rank
            for (int i =0;i<size;i++){
                counters[i][0] = 4 - countOcc(goals[i],0)/2;
                counters[i][1] = countOcc(goals[i],-1)/2;
                // 4-3  - 9
                if (counters[i][0] == 4 && counters[i][1] == 3){counters[i][2]=9;}
                // 4-2  - 8
                else if (counters[i][0] == 4 && counters[i][1] == 2){counters[i][2]=8;}
                // 4-1  - 7
                else if (counters[i][0] == 4 && counters[i][1] == 1){counters[i][2]=7;}
                // 4-0  - 6
                else if (counters[i][0] == 4 && counters[i][1] == 0){counters[i][2]=6;}
                // 3-2  - 5
                else if (counters[i][0] == 3 && counters[i][1] == 2){counters[i][2]=5;}
                // 3-1  - 4
                else if (counters[i][0] == 3 && counters[i][1] == 1){counters[i][2]=4;}
                // 3-0  - 3
                else if (counters[i][0] == 3 && counters[i][1] == 0){counters[i][2]=3;}
                // 2-1  - 2
                else if (counters[i][0] == 2 && counters[i][1] == 1){counters[i][2]=2;}
                // 2-0  - 1
                else if (counters[i][0] == 2 && counters[i][1] == 0){counters[i][2]=1;}
                else {counters[i][2]=0;}
            }
            int offset = 0;
            for (int r=1;r<10;r++){
                int count = 0;
                for (int i =0;i<size;i++){
                    if (counters[i][2]==r){
                        Coordinates coord = new Coordinates(goals[i][0],goals[i][1]);
                        LevelMetadata.goalRank.replace(coord,r-offset);
                        count+=1;
                    }
                }
                if (count==0){offset+=1;}
            }

        }

        private void rankGoalDead() {
            int maxrank = deadend.length;
            int offset = 0;
            for (int r=1;r<maxrank+1;r++){
                int count = 0;
                for (int i=0;i<size;i++){
                    if (maxrank-goals[i][2]==r){
                        Coordinates coord = new Coordinates(goals[i][0],goals[i][1]);
                        LevelMetadata.goalRank.replace(coord,r-offset);
                        count+=1;
                        break;
                    }
                }
                if (count==0){offset+=1;}
            }
        }

        public enum ClusterType{
            ADJACENT,DEADEND;
        }
        private boolean isNotInCluster(Coordinates coord){
            for (int[] list: goals){
                if (list[0]==coord.row && list[1]==coord.col){
                    return false;
                }
            }
            return true;
        }
        public boolean[][] markVisited(boolean[][] visited){
            for (int[] list: goals){
                visited[list[0]][list[1]] = true;
            }
            return visited;
        }
    }

    public static int countOcc(int[] array, int val){
        int count = 0;
        for (int j=0; j < array.length; j++) {
            if (val == array[j]) {
                count += 1;
            }
        }
        return count;
    }

    /*
    Creates the cluster arrays of goals that are "related to each other"
    The data structure is a
    [
        [
        [1,1,true,true,false,false,2,1,true,true]
        ]
    ]
     */
    private GoalCluster[] getGoalClusters(HashMap<Coordinates, Integer> goalRank, boolean[][] walls, char[][] goals) {
        boolean[][] visited = new boolean[State.GOALS.length][State.GOALS[0].length];
        int totalGoals = LevelMetadata.goalRank.size();
        GoalCluster[] goalClusters = new GoalCluster[totalGoals/2 +1];
        for (boolean[] visitedRow:visited){
            Arrays.fill(visitedRow, false);
        }
        for (int row = 1; row < State.GOALS.length - 1; row++) {
            for (int col = 1; col < State.GOALS[row].length - 1; col++) {
                if (StateUtils.isGoal(row,col) && !visited[row][col]){ //if we scan a goal that was not yet visited
                    Coordinates cell = new Coordinates(row,col);
                    List<Coordinates> adjCells = getAdjacentCells(cell);
                    if (isInDeadEnd(cell) != null){
                        int[][] deadend = isInDeadEnd(cell);
                        if (deadEndHas2Goals(deadend)) {
                            GoalCluster cluster = new GoalCluster(cell, deadend, GoalCluster.ClusterType.DEADEND);
                            visited = cluster.markVisited(visited);
                            int i = Arrays.asList(goalClusters).indexOf(null);
                            goalClusters[i] = cluster;
                        }
                    } else {continue;}
                }
            }
        }
        for (int row = 1; row < State.GOALS.length - 1; row++) {
            for (int col = 1; col < State.GOALS[row].length - 1; col++) {
                if (StateUtils.isGoal(row,col) && !visited[row][col]){ //if we scan a goal that was not yet visited
                    Coordinates cell = new Coordinates(row,col);
                    List<Coordinates> adjCells = getAdjacentCells(cell);
                    if (hasGoal(adjCells, visited)) {
                        GoalCluster cluster = new GoalCluster(cell, GoalCluster.ClusterType.ADJACENT);
                        visited = cluster.markVisited(visited);
                        int i = Arrays.asList(goalClusters).indexOf(null);
                        goalClusters[i]=cluster;
                    } else {continue;}
                }
            }
        }
        return goalClusters;
    }
    public boolean deadEndHas2Goals(int[][] deadend) {
        int nbGoals =0;
        for (int[] xy : deadend) {
            if (StateUtils.isGoal(xy[0],xy[1])){
                nbGoals +=1;
            }
        }
        if (nbGoals >=2){return true;}
        else {return false;}
    }

    private boolean hasGoal(List<Coordinates> adjCells, boolean[][] visited) {
        for (Coordinates cell: adjCells){
            if (StateUtils.isGoal(cell.row,cell.col) && !visited[cell.row][cell.col]){return true;}
        }
        return false;
    }
    public static class Coordinates {
        int row;
        int col;

        public Coordinates(int x, int y) {
            this.row = x;
            this.col = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Coordinates that = (Coordinates) o;
            return row == that.row && col == that.col;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, col);
        }
    }

    // The getCorridors method takes a 2D boolean array representing walls and returns a 3D integer array
    // representing the corridors in the map. Each 2D integer array in the 3D array is a corridor.
    public int[][][] getCorridors(boolean[][] walls) {
        int rows = walls.length;
        int cols = walls[0].length;
        boolean[][] visited = new boolean[rows][cols];
        List<int[][]> corridorsList = new ArrayList<>();

        for (int row = 1; row < rows - 1; row++) {
            for (int col = 1; col < cols - 1; col++) {
                if (!walls[row][col] && !visited[row][col]) {
                    List<int[]> currentCorridorList = new ArrayList<>();
                    corridorDFS(row, col, walls, visited, currentCorridorList);
                    if (!currentCorridorList.isEmpty()) {
                        int[][] currentCorridor = new int[currentCorridorList.size()][2];
                        for (int i = 0; i < currentCorridorList.size(); i++) {
                            currentCorridor[i] = currentCorridorList.get(i);
                        }
                        corridorsList.add(currentCorridor);
                    }
                }
            }
        }

        int[][][] corridors = new int[corridorsList.size()][][];
        for (int i = 0; i < corridorsList.size(); i++) {
            corridors[i] = corridorsList.get(i);
        }
        return corridors;
    }


    // from all corridors get the ones with a single entrance (deadend corridors)
    public int[][][] getDeadendCorridors(int[][][] corridors, boolean[][] walls) {
        List<int[][]> deadendCorridorsList = new ArrayList<>();
        
        for (int[][] corridor : corridors) {
            for (int[] cell : corridor) {
                // if cell is surrounded by 3 walls then it is a deadend
                if (surroundedBy3Walls(cell, walls)) {
                    deadendCorridorsList.add(corridor);
                    break;
                }
            }
        }

        // sort each corridor in deadendCorridorsList by distance from deadend to entrance
        // closer to deadend = lower index in list
        deadendCorridorsList = sortDeadendCorridors(deadendCorridorsList, walls);

        int[][][] deadendCorridors = new int[deadendCorridorsList.size()][][];
        for (int i = 0; i < deadendCorridorsList.size(); i++) {
            deadendCorridors[i] = deadendCorridorsList.get(i);
        }

        return deadendCorridors;
    }

    private boolean surroundedBy3Walls(int[] cell, boolean[][] walls) {
        int row = cell[0];
        int col = cell[1];
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}}; // Right, Down, Left, Up
        int numWalls = 0;
        for (int[] direction : directions) {
            int newRow = row + direction[0];
            int newCol = col + direction[1];

            // check if new cell is out of bounds
            if (newRow < 0 || newRow >= walls.length || newCol < 0 || newCol >= walls[0].length) {
                numWalls++;
                continue;
            }

            if (walls[newRow][newCol]) {
                numWalls++;
            }
        }
        return numWalls == 3;
    }

    public List<int[][]> sortDeadendCorridors(List<int[][]> deadendCorridorsList, boolean[][] walls) {
        List<int[][]> sortedCorridors = new ArrayList<>();

        for (int[][] corridor : deadendCorridorsList) {
            List<int[]> sortedCorridor = new ArrayList<>();
            boolean[] visited = new boolean[corridor.length];
            for (int i = 0; i < corridor.length; i++) {
                if (surroundedBy3Walls(corridor[i], walls)) {
                    deadendDFS(corridor, i, visited, sortedCorridor);
                    break;
                }
            }

            // invert the order of the sortedCorridor before adding it to sortedCorridors
            // so that the deadend is at index 0
            List<int[]> invertedSortedCorridor = new ArrayList<>();
            for (int i = 0; i < sortedCorridor.size(); i++) {
                invertedSortedCorridor.add(sortedCorridor.get(sortedCorridor.size() - 1 - i));
            }

            sortedCorridors.add(invertedSortedCorridor.toArray(new int[0][]));
        }

        return sortedCorridors;
    }

    private void deadendDFS(int[][] corridor, int cellIndex, boolean[] visited, List<int[]> sortedCorridor) {
        visited[cellIndex] = true;
        sortedCorridor.add(0, corridor[cellIndex]);

        // For each neighboring cell of the current cell
        for (int i = 0; i < corridor.length; i++) {
            if (!visited[i] && isNeighbor(corridor[cellIndex], corridor[i])) {
                deadendDFS(corridor, i, visited, sortedCorridor);
            }
        }
    }

    public static int[][] isInDeadEnd(Coordinates coord){
        for (int[][] deadend: LevelMetadata.DEADEND_CORRIDORS){
            for (int[] xy: deadend){
                if (xy[0] == coord.row&&xy[1]==coord.col){
                    return deadend;
                }
            }
        }
        return null;
    }


    // The corridorDFS method is a Depth-First Search (DFS) used to explore and build the corridors.

    private boolean isNeighbor(int[] cell1, int[] cell2) {
        return Math.abs(cell1[0] - cell2[0]) + Math.abs(cell1[1] - cell2[1]) == 1;
    }

    // The corridorDFS method is a DFS used to explore and build the corridors.
    // It takes the current row, col, walls array, visited array, and the list of cells in the current corridor.
    private void corridorDFS(int row, int col, boolean[][] walls, boolean[][] visited, List<int[]> currentCorridor) {
        if (visited[row][col]) {
            return;
        }
    
        visited[row][col] = true;
    
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}}; // Right, Down, Left, Up
        int numWalls = 0;
        int numCorridorNeighbors = 0;
    
        for (int[] direction : directions) {
            int newRow = row + direction[0];
            int newCol = col + direction[1];
    
            if (walls[newRow][newCol]) {
                numWalls++;
            } else if (isCorridorCell(newRow, newCol, walls)) {
                numCorridorNeighbors++;
            }
        }
    
        boolean isTOrCrossSection = numCorridorNeighbors > 2;
        int numTOrCrossSections = numTOrCrossSections(row, col, walls);
    
        if (numWalls >= 2 && !isTOrCrossSection && !isOpenCorner(row, col, walls)
            && (numCorridorNeighbors >= 1 || numTOrCrossSections == 2)) {

            currentCorridor.add(new int[]{row, col});
        }
    
        // TODO refactor
        if (!isTOrCrossSection) {
            for (int[] direction : directions) {
                int newRow = row + direction[0];
                int newCol = col + direction[1];
                int innerNumWalls = 0;

                for (int[] innerDirection : directions) {
                    int innerNewRow = row + innerDirection[0];
                    int innerNewCol = col + innerDirection[1];
            
                    if (walls[innerNewRow][innerNewCol]) {
                        innerNumWalls++;
                    }
                }
    
                if (!walls[newRow][newCol] && !visited[newRow][newCol] && innerNumWalls > 1) {
                    corridorDFS(newRow, newCol, walls, visited, currentCorridor);
                }
            }
        }
    }


    // TODO missing condition that to be a corridor cell it must have at least 1 adjacent corridor cell
    private boolean isCorridorCell(int row, int col, boolean[][] walls) {
        if (coordsOutOfBounds(new Coordinates(row, col))) {
            return false;
        }

        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        int numWalls = 0;
    
        for (int[] direction : directions) {
            int newRow = row + direction[0];
            int newCol = col + direction[1];

            if (coordsOutOfBounds(new Coordinates(newRow, newCol))) {
                continue;
            }
    
            if (walls[newRow][newCol]) {
                numWalls++;
            }
        }
    
        return numWalls >= 2;
    }


    private int numTOrCrossSections(int row, int col, boolean[][] walls) {
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}}; // Right, Down, Left, Up
        int count = 0;
    
        for (int[] direction : directions) {
            int newRow = row + direction[0];
            int newCol = col + direction[1];

            if (coordsOutOfBounds(new Coordinates(newRow, newCol))) {
                continue;
            }
    
            if (!walls[newRow][newCol]) {
                int corridorNeighbors = 0;
                for (int[] innerDirection : directions) {
                    int innerNewRow = newRow + innerDirection[0];
                    int innerNewCol = newCol + innerDirection[1];

                    if (coordsOutOfBounds(new Coordinates(innerNewRow, innerNewCol))) {
                        continue;
                    }
    
                    if (!walls[innerNewRow][innerNewCol] && isCorridorCell(innerNewRow, innerNewCol, walls)) {
                        corridorNeighbors++;
                    }
                }
                if (corridorNeighbors > 2) {
                    count++;
                }
            }
        }
    
        return count;
    }    


    private boolean isOpenCorner(int row, int col, boolean[][] walls) {
            
        // if Up, Up-right, and Right are free, then it's an open corner
        if (!walls[row - 1][col] && !walls[row - 1][col + 1] && !walls[row][col + 1]) {
            return true;
        }

        // if Up, Up-left, and Left are free, then it's an open corner
        if (!walls[row - 1][col] && !walls[row - 1][col - 1] && !walls[row][col - 1]) {
            return true;
        }

        // if Down, Down-right, and Right are free, then it's an open corner
        if (!walls[row + 1][col] && !walls[row + 1][col + 1] && !walls[row][col + 1]) {
            return true;
        }
        
        // if Down, Down-left, and Left are free, then it's an open corner
        if (!walls[row + 1][col] && !walls[row + 1][col - 1] && !walls[row][col - 1]) {
            return true;
        }
        
        return false;
    }
    

}
