package Navigation;

import lejos.hardware.motor.*;
import wallFollower.UltrasonicController;

public class coordinateFollower implements UltrasonicController{

	private final int bandCenter, bandwidth;
	private final int motorLow, motorHigh, FILTER_OUT = 20;
	public static final int DELTASPD = 100;
	private int distance;
	private int distError;
	private int filterControl;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;

	public coordinateFollower(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
			int bandCenter, int bandwidth, int motorLow, int motorHigh) {

		//Default Constructor
		this.bandCenter = bandCenter;
		this.bandwidth = bandwidth;
		this.motorLow = motorLow;
		this.motorHigh = motorHigh;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;

		leftMotor.setSpeed(motorHigh);				// Start robot moving forward
		rightMotor.setSpeed(motorHigh);
		leftMotor.forward();
		rightMotor.forward();
		filterControl = 0;
	}

	@Override
	public void processUSData(int distance) {

		// rudimentary filter - tosses out invalid samples corresponding to null signal.
		
		if (distance >= 255 && filterControl < FILTER_OUT) {
			// bad value, do not set the distance var, however do increment the
			// filter value
			filterControl++;
		} 
		else if (distance >= 255) {
			// We have repeated large values, so there must actually be nothing
			// there: leave the distance alone
			this.distance = distance;
		} 	
		else {
			// distance went below 255: reset filter and leave
			// distance alone.
			filterControl = 0;
			this.distance = distance;
		}

		// Main control loop: read distance, determine error, adjust speed, and repeat
					
			distError=bandCenter-this.distance;			// Compute error using filter distance
			System.out.println(distError);				//	prints the distError on the ev3 display for debugging 

			if (Math.abs(distError) <= bandwidth) {	// Within limits of acceptable values of bandCenter, the robot goes straight
				leftMotor.setSpeed(motorHigh);		
				rightMotor.setSpeed(motorHigh);
				leftMotor.forward();
				rightMotor.forward();				
			}
			else if (distError > 0) {				// Too close to the wall
				
				if (distError>=25){					//Critical point(very close to the wall), the robot will go backwards to avoid possible collision
					leftMotor.setSpeed(motorLow);
					rightMotor.setSpeed(motorLow);
					leftMotor.backward();
					rightMotor.backward();						
				}
				else if(distError>=15){				//Sharp turn before getting to the critical point, left motor set faster and right motor set backwards
					leftMotor.setSpeed(motorHigh+2*DELTASPD);	//in order to make a sharp right turn
					rightMotor.setSpeed(motorLow+DELTASPD/4);
					leftMotor.forward();
					rightMotor.backward();						
				}
				else{	
					leftMotor.setSpeed(motorHigh+DELTASPD);		//normal right turn, below the acceptable values of bandCenter
					rightMotor.setSpeed(motorLow-DELTASPD/2);	
					leftMotor.forward();
					rightMotor.backward();		
					}
			}
			else if (distError < 0) {				// Too far from the wall
				if(distError<-20){					//when the robot is very far from the wall, the turn will be sharper to the left
					leftMotor.setSpeed(motorLow-25);
					rightMotor.setSpeed(motorHigh+DELTASPD);
					leftMotor.forward();
					rightMotor.forward();		
				}
				else{
					leftMotor.setSpeed(motorLow);	//normal far distance from the wall, robot turns left
					rightMotor.setSpeed(motorHigh+DELTASPD/2);
					leftMotor.forward();
					rightMotor.forward();	
				}				
			}					
	}
	
	@Override
	public int readUSDistance() {
		return this.distance;
	}
}