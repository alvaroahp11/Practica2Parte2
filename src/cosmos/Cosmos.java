package cosmos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import manageStates.*;
import exceptions.CosmosException;
import searchAlgorithms.*;

public class Cosmos {

	public static void checkArguments(String args[]) throws CosmosException {
		// Only 2 arguments
		if (args.length != 2) {
			throw new CosmosException("\tThe structure must be:\n ./cosmos.java <problema.prob> <heuristica>");
		} else if (!args[1].equals("heuristica1") && !args[1].equals("heuristica2")) { // La heuristica esta definida
			throw new CosmosException("La heuristica no esta definida");
		}

	}

	public static String[] readFile(File f) throws CosmosException {
		String data[] = new String[3];
		try {
			Scanner myReader = new Scanner(f);

			for (int i = 0; myReader.hasNextLine() == true && i < 3; i++) {
				data[i] = myReader.nextLine();
			}
			myReader.close();
		} catch (FileNotFoundException e) {
			throw new CosmosException("Cant find the file: " + f.getPath());
		}
		return data;
	}
	//Comprobamos que tiene el formato y eliminamos lo que va antes de cada linea
	public static String[] dataFilter(String data[]) throws CosmosException {

		for (int i = 0; i < data.length; i++) {
			if (data[i].equals(null)) {
				throw new CosmosException("Wrong input format \n\tMust be like: \n" + "OBS: (i,j);(i,j); ... ;(i,j)\r\n"
						+ "SAT1: 1;1;1;1;nbattery\r\n" + "SAT2: 1;1;1;1;nbattery");
			}
			String line[] = data[i].split(":");
			if (!((line[0].equals("OBS") && i == 0) || (line[0].equals("SAT1") && i == 1)
					|| (line[0].equals("SAT2") && i == 2))) {
				throw new CosmosException("Wrong input format \n\tMust be like: \n" + "OBS: (i,j);(i,j); ... ;(i,j)\r\n"
						+ "SAT1: 1;1;1;1;nbattery\r\n" + "SAT2: 1;1;1;1;nbattery");
			}
			data[i] = line[1];
		}
		return data;
	}

	public static State firstState(String data[], String typeOfHeuristic) {
		Satelite SAT1 = null;
		Satelite SAT2 = null;
		String area[][] = new String[4][12];
			
		//Aqu� se extraen los valores de las coordenadas 
		String coordinate[] = data[0].split(";");	
		for (int j = 0; j < coordinate.length; j++) {
			int start = (coordinate[j].indexOf("(") + 1);
			int end = coordinate[j].indexOf(",");			
			int x = Integer.parseInt(coordinate[j].substring(start, end));

			coordinate[j] = coordinate[j].substring(end + 1);
			end = coordinate[j].indexOf(")");
			int y = Integer.parseInt(coordinate[j].substring(0 , end));
			area[x][y] = "O" + (j+1);
		}	
		
		
		
		//aqu� se extraen las configuraciones del satelite 1  satData[0] -> observationCost ; satData[1] -> transmitionCost ; satData[2] -> spinCost ; satData[3] -> chargeUnit, satData[4] -> battery
		String satData[] = data[1].split(";"); 
		
	    //quitar espacios en todos los satData[]
		for(int i = 0; i < satData.length; i++) {
		    satData[i] = satData[i].replaceAll(" ", "");
		}
		SAT1 = new Satelite(Integer.parseInt(satData[4]), Integer.parseInt(satData[0]), Integer.parseInt(satData[1]), Integer.parseInt(satData[2]),Integer.parseInt(satData[3]));
		
		//aqu� se extraen las configuraciones del satelite 2  satData[0] -> observationCost ; satData[1] -> transmitionCost ; satData[2] -> spinCost ; satData[3] -> chargeUnit, satData[4] -> battery
		satData = null;
		satData = data[2].split(";");
		
		//quitar espacios en todos los satData[]
		for(int i = 0; i < satData.length; i++) {
            satData[i] = satData[i].replaceAll(" ", "");
        }
		SAT2 = new Satelite(Integer.parseInt(satData[4]), Integer.parseInt(satData[0]), Integer.parseInt(satData[1]), Integer.parseInt(satData[2]),Integer.parseInt(satData[3]));

		
		if(typeOfHeuristic.equals("heuristica1"))
		    return new State(SAT1, SAT2, area, true);
		else
		    return new State(SAT1, SAT2, area, false);
	}
	
	public static String jobDone (State a) {
        String job = a.getParentAction();
        job = job.substring(4);
        job = job.replace("Sat2", ";");
        
        return job;
    }
	

    
    public static int writeOutput(ArrayList<State> camino) throws IOException {
        camino.remove(camino.size()-1);
        int i = 1;
        String [] aux = null;
        new File("./output").mkdirs();
        File myObj = new File("./output/problema.prob.output");
        FileWriter myWriter = new FileWriter(myObj);
        while(!camino.isEmpty()) {
            String SatsContent = null;           
            if(camino.get(camino.size()-1).getJ() == 0 && camino.get(camino.size()-1).getParent().getJ() == 11){
                SatsContent = jobDone(camino.remove(camino.size()-1));
                aux = SatsContent.split(";");
                for (int j = 0; j < 12; j++) {
                    myWriter.write(i+". SAT1: "+"Nothing"+", SAT2: "+"Nothing"+"\n");
                    i++;
                }
            }else {
                SatsContent = jobDone(camino.remove(camino.size()-1));
                aux = SatsContent.split(";");
                myWriter.write(i+". SAT1: "+aux[0]+", SAT2: "+aux[1]+"\n");
                i++;
            }
        }
        myWriter.close();
		return i-1;
    }
    
	public static void writeStatistics(long start, AStarAlgorithm aStar, int longPlan, long finish) {
		try {
			File myObj = new File("./output/problema.prob.statistics");
        	FileWriter myWriter = new FileWriter(myObj);
			myWriter.write("Tiempo total: "+ (finish-start) +"\nCoste total: "+longPlan+"\nLongitud del plan: "+longPlan+"\nNodos expandidos: "+aStar.getExpandedNodes());
			myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	public static void main(String args[]) throws CosmosException {
	    long start = System.currentTimeMillis();
		// Check the arguments
		checkArguments(args);

		// Open the file
		File input;
		try {
			input = new File(args[0]);
		} catch (NullPointerException e) {
			throw new CosmosException("Input File is null");
		}
		// Comprobar que el file existe
		if (!input.exists()) {
			throw new CosmosException("The file " + input.getPath() + " doesnt exists");
		}

		// Data from file
		String data[] = readFile(input);

		// Check syntax of file
		try {
			dataFilter(data);

		} catch (CosmosException e) {
			throw e;
		}

		State initialState = firstState(data,args[1]);

		AStarAlgorithm aStar = new AStarAlgorithm();

		State finalState = aStar.aStartInit(initialState);
		
		//States to complete the job
        ArrayList<State> camino = finalState.getPath();
        
		//Long Plan && Coste total
		int longPlan = 0;
		//Create output
		
        try {
            longPlan = writeOutput(camino);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        long finish = System.currentTimeMillis();

        writeStatistics(start, aStar, longPlan, finish);
       
	
		



	}



}
