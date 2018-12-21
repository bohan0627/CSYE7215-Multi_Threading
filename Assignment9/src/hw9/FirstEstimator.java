package hw9;

import akka.actor.ActorRef;
import java.io.File;

import akka.actor.UntypedActor;

/**
 * @author Bo Han
 * This is the FirstEstimator actor
 * At the begining, it provides the defalut estimation to Counter actor
 * Then each time: re-calculate the estimation value besed on the feedback sent by Counter actor
 */
public class FirstEstimator extends UntypedActor {

    private String estimatior;
    private double cValue;
    final private double gValue;
    private ActorRef feedbackProvider = null;
    private double tempValue;

    public FirstEstimator() {
        this.estimatior = "FirstEstimator";
        this.cValue = 0.3;
        this.gValue = 0.7;
        tempValue = 0.0;
    }

    @Override
    public void onReceive(Object msg) throws Throwable {
        if(msg instanceof Messages.Feedback){
            feedbackProvider = getSender();
            tempValue = cValue + ((Messages.Feedback) msg).getFeedback();

            //new estimation: cValue
            cValue = cValue * gValue + tempValue * (1 - gValue);
            feedbackProvider.tell(new Messages.Estimation(estimatior,cValue), getSelf());
        }

        else if(msg instanceof Messages.FirstInitilization){
            feedbackProvider = getSender();
            feedbackProvider.tell(new Messages.Estimation(estimatior,cValue), getSelf());
        }
    }
}