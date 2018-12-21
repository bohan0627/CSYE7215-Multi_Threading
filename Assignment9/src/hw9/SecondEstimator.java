/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hw9;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

/**
 * @author Bo Han
 * This is the SecondEstimator actor
 * Its methods and variables are similar to the ones in the FirstEstimator.
 * The main difference is the default cValue and gValue
 * At the begining, it provides the defalut estimation to Counter actor
 * Then each time: re-calculate the estimation value besed on the feedback sent by Counter actor
 */
public class SecondEstimator extends UntypedActor {
        
    private String estimatior;
    private double cValue;
    private double gValue;
    private ActorRef feedbackProvider = null;
    private double tempValue;

	public SecondEstimator() {
	    this.estimatior = "SecondEstimator";
	    this.cValue = 0.4;
	    this.gValue = 0.5;
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
