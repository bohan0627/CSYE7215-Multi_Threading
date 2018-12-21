package hw9;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;
import java.util.concurrent.TimeUnit;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.List;

/**
 * @author Bo Han
 * Main class for your estimation actor system.
 */
public class User {

	public static void main(String[] args) throws Exception {
		ActorSystem system = ActorSystem.create("EstimationSystem");
                
                //You could change this filePath below to read other files
                //Sometimes, due to the files, this program may run slow, please test few .txt files if possible
		ActorRef counter = system.actorOf(Props.create(FirstCounter.class, "./Akka_Text/"));

		final Timeout timeout = new Timeout(Duration.create(120, TimeUnit.SECONDS));

		final Future<Object> future = Patterns.ask(counter, new Messages.StartProcessingFiles(), timeout);
                
                Messages.Finish result = (Messages.Finish) Await.result(future, timeout.duration());

                List<FileEstimationInfo> estimations = result.getEstimationResult();
                for(FileEstimationInfo e : estimations){
                    System.out.println("FileName: " + e.getFileName() + " Vowel Nums: " + e.getVowelsInFile() + " Letter Nums: " + e.getLetterNumbersInFile() + " FirstEstimatorError: " + e.getEstimatorOneDiff() +
                            " SecondEstimatorError: " + e.getEstimatorTwoDiff());
                }
                
                //System.out.println("Estimation System General Info");
                System.out.println("First Estimator average error: " + result.getAverageErrorForOne());
                System.out.println("Second Estimator average error: " + result.getAverageErrorForTwo());

                system.terminate();
		/*
		 * Create the FirstEstimator Actor and send it the StartProcessingFolder
		 * message. Once you get back the response, use it to print the result.
		 * Remember, there is only one actor directly under the ActorSystem.
		 * Also, do not forget to shutdown the actorsystem
		 */

	}

}
