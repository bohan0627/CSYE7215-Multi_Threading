package hw9;

import akka.actor.UntypedActor;
import java.lang.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Bo Han
 * This file is used for calculating the vowels and letter numbers in each line and tell the FileReader actor after finish
 */
public class LineReader extends UntypedActor{
    
    private int vowelNums;
    private int letterNums;
    private char[] letters;
    Set<Character> s;

    public LineReader() {
        this.vowelNums = 0;
        this.letterNums = 0;
        s = new HashSet<>();
        s.add('A');
        s.add('E');
        s.add('I');
        s.add('O');
        s.add('U');
        s.add('Y');
    }
    
    
    @Override
    public void onReceive(Object message) throws Throwable {
        
        if (message instanceof Messages.LineReading) {
			String line = ((Messages.LineReading) message).getLine();
                        letters = line.toCharArray();
                        for(char c: letters){
                            c = Character.toUpperCase(c);
                            if(Character.isLetter(c)){
                                letterNums++;
                                if(s.contains(c))
                                    vowelNums++;
                            }
                        }
			getSender().tell(new Messages.NumbersInLine(vowelNums,letterNums), getSelf());
		} else {
			System.err.println("Cannot identify message" + message);
		}
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
