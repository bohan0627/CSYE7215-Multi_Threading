package hw9;

import java.util.List;

/**
 * @author Bo Han
 * Messages that are passed around the actors are usually immutable classes.
 * Think how you go about creating immutable classes:) Make them all static
 * classes inside the Messages class.
 * 
 * This class should have all the immutable messages that you need to pass
 * around actors. You are free to add more classes(Messages) that you think is
 * necessary
 */
public class Messages {
        
        
    public static class StartProcessingFiles {
        //This is used for telling Counter to read all the files in the given folder
    }
    
	public static class StartReadingFile {
        //This is used for telling FileReader to read all the lines in the given file
	}
        
	public static class FirstInitilization{
        //This is used for telling two Estimators to send the default C value
    }

    public static class LineReading {
        //This is used for keeping each line info
        private String line;

		public LineReading(String line) {
			this.line = line;
		}

		public String getLine() {
			return line;
		}
	}
        
	public static class NumbersInLine {
        //This is used for keeping vowels and letter nums
                private int vowels;
		private int letterNumbers;

		public NumbersInLine (int vowels, int letterNumbers) {
		    this.vowels = vowels;
		    this.letterNumbers = letterNumbers;
		}

		public int getVowelsInLine() {
		    return vowels;
		}
		public int getLetterNumbersInLine(){
		    return letterNumbers;
		}
	}
        
	public static class NumsInFile {
        private String fileName;
        private int vowels;
        private int letterNumbers;

        public NumsInFile(String fileName, int vowels, int letterNumbers) {
            this.fileName = fileName;
            this.vowels = vowels;
            this.letterNumbers = letterNumbers;
        }

        public int getVowelsInFile() {
            return vowels;
        }

        public int getLetterNumbersInFile() {
            return letterNumbers;
        }

        public String getFileName() {
            return fileName;
        }
    }

    public static class Feedback{
        private double feedback;
            
        public Feedback(double feedback){
            this.feedback = feedback;
        }
            
        public double getFeedback(){
            return  feedback;
        }
    }
        
    public static class Estimation{
        private String estimator;
        private double estimation;
            
        public Estimation(String estimator, double estimation) {
            this.estimator = estimator;
            this.estimation = estimation;
        }
        
        public double getEstimation(){
            return estimation;
        }
        
        public String getEstimationName(){
            return estimator;
        }
              
      }
    public static class Finish{
        private List<FileEstimationInfo> res;

        public Finish(List<FileEstimationInfo> res) {
            this.res = res;
        }
        
        public List<FileEstimationInfo> getEstimationResult(){
            return res;
        }
        
        public double getAverageErrorForOne(){
                        int count = 0;
                        double error = 0.0;
                        for(FileEstimationInfo s: res){
                                error += s.getEstimatorOneDiff();
                                count++;
                        }
                        return error/count;
                }

                 public double getAverageErrorForTwo(){
                        int count = 0;
                        double error = 0.0;
                        for(FileEstimationInfo s: res){
                                error += s.getEstimatorTwoDiff();
                                count++;
                        }
                        return error/count;
                }
        //Also could add some helper methods to calculate each estimator's estimation info
        
    }
        
   
        
        
}