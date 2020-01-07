import java.io.FileInputStream;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.io.IOException;
import java.util.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class WordReader {

	
	public static void main(String[] args) throws IOException, Exception {
		
		// Create Input & Scanner for the security_txt
		FileInputStream fis = new FileInputStream("security_txt.txt");
		Scanner fileInput = new Scanner(fis);		
		
		//Create ArrayLists
		ArrayList<String> words = new ArrayList<String>();
		ArrayList<String> safety = new ArrayList<String>();
	
		//while loop to make sure there is a value to enter from securty_txt.txt
		//& adding the value to words arraylist if present
		while(fileInput.hasNext()) {
			words.add(fileInput.next());
		}
		
		//Read Through File & Find Words

			for(int i = 0; i<words.size();i++) {
				//filtering out words that are 2 letters or less
				if(words.get(i).length()>2) {
					//Get Next Word
					String word = words.get(i);
					
					//Convert Word to Lowercase 
					String lowerCaseWord = word.toLowerCase();
					
					//Replace , . ? ) ( " with Space
					String periodFreeWord = lowerCaseWord.replaceAll("\\.", " ");
					String commaFreeWord = periodFreeWord.replaceAll(",", " ");
					String questionFreeWord = commaFreeWord.replaceAll("\\?", " ");
					String leftParenFreeWord = questionFreeWord.replaceAll("\\(", " ");
					String rightParenFreeWord = leftParenFreeWord.replaceAll("\\)", " ");
					String quoteFreeWord = rightParenFreeWord.replaceAll("\"", " ");
					String colonFreeWord = quoteFreeWord.replaceAll(":", " ");
					String charFreeWord = colonFreeWord.replaceAll(";", " ");
					
					//Trim Space from Word
					String nextWord = charFreeWord.trim();	
					
					//adds the trimmed work into the safety arraylist
					safety.add(nextWord);
				}
			}
						
		//Close
		fileInput.close();
		fis.close();

		//create new input and scanner for the junk text file
		FileInputStream fis2 = new FileInputStream("junk.txt");
		Scanner fileInput2 = new Scanner(fis2);		
		
		//Create ArrayLists
		ArrayList<String> junk = new ArrayList<String>();
	
		//while loop to make sure there is a value to enter from junk.txt
		//& adding the value to junk arraylist if present
		while(fileInput2.hasNext()) {
			junk.add(fileInput2.next());
		}
		
		
		//close
		fileInput2.close();
		fis2.close();
		
		//establishing connection for sql
		SimpleDataSource.init("database.properties");
		//checking for connection
		try(Connection conn = SimpleDataSource.getConnection())
		{	
			//Connecting to sql table for the safety table creation
			Statement stat = conn.createStatement();
			//checks if the table Test exists already, this makes it possible to run and re-run the program error free
			try{
		           stat.execute("DROP TABLE Test");
		    }
		    catch(SQLException e){
		         // gets exception if table doesn't exist yet
		    } 
			

			//creating table
			stat.execute("CREATE TABLE Test (word VARCHAR(1000))"); 
			
			
			//parsing through the safety arraylist and entering the values at i into the table
			for (int i = 0; i < safety.size(); i++) {
				//creating sql statement to enter values into Test table
				try(PreparedStatement enterSafety = conn.prepareStatement("INSERT INTO Test VALUES (?)")){
					//enters the value from safety at i into the table
					enterSafety.setString(1, safety.get(i));
					//executing the update
					enterSafety.executeUpdate();
				}
			}			
			
			

		}
		
			//checks if the conn has already been made
			try(Connection conn = SimpleDataSource.getConnection())
			{		
				//checks if the table Test exists already, this makes it possible to run and re-run the program error free
				Statement stat = conn.createStatement();
				
				try{
			           stat.execute("DROP TABLE junkT");
			    }
			    catch(SQLException e){
			         // gets exception if table doesn't exist yet
			    }
								
				
				//creating table to input junk text values into							
				stat.execute("CREATE TABLE junkT (jword VARCHAR(1000))"); 
				
				
				//parsing through the junk arraylist and entering the values at i into the table		
				for (int i = 0; i < junk.size(); i++) {
					//creating sql statement to enter values into junkT table
					try(PreparedStatement enterJunk = conn.prepareStatement("INSERT INTO junkT VALUES (?)")){
						//enters the value from junk at i into the table
						enterJunk.setString(1, junk.get(i));
						//executing the update
						enterJunk.executeUpdate();
					}
				}
				
			//writing query to execute the following steps:
			//1. Selecting the words (Test.word) and the counts (count(Test.word) as word_count) of the words from the database
			//2. left joining values from Test to junkT, only keeping the values that are not present in junkT
			//3. LEFT JOIN junkT  ON Test.word = junkT.jword where junkT.jword is NULL
			//4. group by word to enable count(Test.word)
			//5. Ordering by word_count
			String query = "SELECT Test.word, count(Test.word) as word_count FROM Test LEFT JOIN junkT  ON Test.word = junkT.jword where junkT.jword is NULL group by word order by word_count";
			//Executing the query to get a ResultSet returned as result
			ResultSet result = stat.executeQuery(query);
			
			
			System.out.print("Word,Count");
			//while loop to go through result and end when it is complete
			while (result.next()) {  
				//for loop to parse through result to print values in each column, 
				for(int i=1; i<=2; i++){
					
					//printing the string in column i
					if(i==1) {
						System.out.print(result.getString(i) + ",");
					}
					else {
						System.out.print(result.getString(i));
					}
					
				}		
			    //printing empty line
				System.out.println();
			
			}
			//closing ResultSet
			result.close();
		}
	}

	}
