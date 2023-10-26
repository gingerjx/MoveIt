package searchclient;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

/*
    Used in case debugging is needed.
 */
public class Debug {

    /*
     * Enables running algorithm with the debugger
     */
    public static void debug(String levelName) throws IOException {
        // Parse the level.
        BufferedReader serverMessages = new BufferedReader(new FileReader("../" + levelName));
        State initialState = SearchClient.parseLevel(serverMessages);
        Frontier frontier = new FrontierBestFirst(new HeuristicAStar(initialState));

        // Search for a plan.
        Action[][] plan = SearchClient.search(initialState, frontier);
        for (Action[] jointAction : plan) {
            System.out.print(jointAction[0].name);
            for (int action = 1; action < jointAction.length; ++action) {
                System.out.print("|");
                System.out.print(jointAction[action].name);
            }
            System.out.println();
        }
    }

    public static void printSplits(int mapRows, int mapCols, int[][] mapSplit) {
        System.err.println("Debugging map split: ");
        for (int x=0; x<mapRows; x++){
            for (int y=0; y<mapCols;y++){
                System.err.printf(" %d |",mapSplit[x][y]);
            }
            System.err.println("");
        } 
    }

    public static void printWalls(State initialState){
        int rows = LevelMetadata.MAP_ROWS;
        int cols =LevelMetadata.MAP_COLS;
        
        System.err.println("Walls for state replaced state");
        for (int x=0; x<rows; x++){
            for (int y=0; y<cols;y++){
                if (State.WALLS[x][y]==true){
                    System.err.print(" + |");
                } else{
                    System.err.print("   |");
                }
                
            }
            System.err.println("");
        } 
    }

    /*
     * Translates int[][] paths to more readable String[]
     */
    public static String[] pathsToStrings(int[][] paths) {
        String[] strings = new String[paths.length];

        for (int i=0; i<paths.length; i++) {
            strings[i] = "[";
            for (int j=0; j<paths[i].length; j += 2) {
                strings[i] += "(" + paths[i][j] + ", " + paths[i][j+1] + ") . ";
            }
            strings[i] += "]";
        }

        return strings;
    }

    public static void printCorridors(int[][][] corridors) {
        for (int i = 0; i < corridors.length; i++) {
            int[][] corridor = corridors[i];
            System.err.println("--- Corridor " + i + ":");
            for (int[] cell : corridor) {
                System.err.println(Arrays.toString(cell));
            }
            System.err.println("-------------------");
        }
    }
    
}
