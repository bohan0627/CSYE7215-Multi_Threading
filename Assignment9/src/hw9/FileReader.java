/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hw9;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import java.io.BufferedReader;
import java.io.File;


/**
 * @author Bo Han
 * This actor is used to processing all the files in the folder
 * Creating LineReader actor to do the calculation
 * Sum all the info in each line and tell the Counter actor
 */
public class FileReader extends UntypedActor{

    private String filePath;
    private int vowels;
    private int letterNumbers;
    private int linesInFile;
    private int linesProcessed;
    private ActorRef fileProvider = null;
    private String line = null;

    public FileReader(String filePath) {
        this.filePath = filePath;
        vowels = 0;
        letterNumbers = 0;
        linesInFile = 0;
        linesProcessed = 0;
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        if(message instanceof Messages.StartReadingFile){
            fileProvider = getSender();

            try (BufferedReader reader = new BufferedReader(new java.io.FileReader(filePath))) {
                line = reader.readLine();
				while(line != null) {
					getContext().actorOf(Props.create(LineReader.class)).tell(new Messages.LineReading(line),getSelf());
					linesInFile++;
					line = reader.readLine();
				}
			}
		}
        
        else if(message instanceof Messages.NumbersInLine){
            vowels += ((Messages.NumbersInLine) message).getVowelsInLine();
            letterNumbers += ((Messages.NumbersInLine) message).getLetterNumbersInLine();
            linesProcessed += 1;
            if(linesProcessed == linesInFile)
                fileProvider.tell(new Messages.NumsInFile(new File(filePath).getName(),vowels, letterNumbers), getSelf());
        }
        else
            System.err.println("Exception happened" + message);
    }

}
