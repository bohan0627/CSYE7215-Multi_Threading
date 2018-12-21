package hw9;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import java.io.File;
import java.util.*;

/**
 * this actor reads the file, counts the vowels and sends the result to
 * Estimator. 
 *
 * @author Bo Han
 *
 */
public class FirstCounter extends UntypedActor {
        
    final private File filesFolder;
    final private int filesNumbers;
        
    private int oneCount;
    private int twoCount;

    private ActorRef estimator1;
    private ActorRef estimator2;
    private ActorRef estimationProvider = null;
    private ActorRef folderProvider = null;

    private List<FileEstimationInfo> res;
        
        
	public FirstCounter(String path) {
	    // TODO Auto-generated constructor stub
        this.filesFolder = new File(path);
		this.filesNumbers = filesFolder.listFiles().length - 1;
		oneCount = 0;
		twoCount = 0;
        res = new ArrayList<>();
	}

	@Override
	public void onReceive(Object message) throws Throwable {
	    if(message instanceof Messages.StartProcessingFiles){
	        this.folderProvider = getSender();
	        Arrays.stream(filesFolder.listFiles()).filter(file -> file.getName().endsWith(".txt"))
				.forEach(file -> getContext().actorOf(Props.create(FileReader.class, file.getAbsolutePath()))
                .tell(new Messages.StartReadingFile(), getSelf()));

	            //Create two Estimator actors
                estimator1 = getContext().actorOf(Props.create(FirstEstimator.class));
                estimator2 = getContext().actorOf(Props.create(SecondEstimator.class));
	    }

	    else if(message instanceof Messages.Estimation){
	        estimationProvider = getSender();
	        String estimator = ((Messages.Estimation) message).getEstimationName();

	        if(estimator.equals("FirstEstimator")){
	            double feedback = res.get(oneCount).getPercent() - ((Messages.Estimation) message).getEstimation();
                res.get(oneCount).setEstimatorOneDiff(feedback);
	            oneCount++;

	            if(oneCount < filesNumbers)
	                estimationProvider.tell(new Messages.Feedback(feedback), getSelf());

	        }
            else if(estimator.equals("SecondEstimator") ){
	            double feedback = res.get(twoCount).getPercent() - ((Messages.Estimation) message).getEstimation();
                res.get(twoCount).setEstimatorTwoDiff(feedback);
                twoCount++;

                if(twoCount < filesNumbers)
                    estimationProvider.tell(new Messages.Feedback(feedback), getSelf());
	        }
                   
	        if(oneCount == (filesNumbers - 1) && twoCount == (filesNumbers - 1))
	            folderProvider.tell(new Messages.Finish(res), folderProvider);
	    }

        else if(message instanceof Messages.NumsInFile){
	        String fileName = ((Messages.NumsInFile) message).getFileName();

            FileEstimationInfo e = new FileEstimationInfo(fileName);
            int vowelsInFile = ((Messages.NumsInFile) message).getVowelsInFile();
            int letterNumbersInFile = ((Messages.NumsInFile) message).getLetterNumbersInFile();

            e.setFileNumbers(vowelsInFile, letterNumbersInFile);
            res.add(e);

            //Telling two estimators to initialize
            if(res.size() == filesNumbers){
                estimator1.tell(new Messages.FirstInitilization(), getSelf());
                estimator2.tell(new Messages.FirstInitilization(), getSelf());
            }
                         
		}
	}
        

}
