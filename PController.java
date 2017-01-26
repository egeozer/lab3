package Navigation;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class PController implements UltrasonicController {
	
	private final int bandCenter, bandwidth;
	private final int motorStraight = 150, FILTER_OUT = 20;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private int distance;
	private int filterControl;
	private int distError;
	int positiveErrorDist;
	
	public PController(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, int bandCenter, int bandwidth) {
		
		//Default Constructor
		this.bandCenter = bandCenter;
		this.bandwidth = bandwidth;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		
		leftMotor.setSpeed(motorStraight);					// Initialize motor rolling forward
		rightMotor.setSpeed(motorStraight);
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
		
			distError=bandCenter-this.distance;		// Compute error
			System.out.println(distError);			// prints the distError on the ev3 display for debugging

			if (Math.abs(distError) <= bandwidth) {	// Within limits, same speed
				leftMotor.setSpeed(2*motorStraight);	
				rightMotor.setSpeed(2*motorStraight);
				leftMotor.forward();
				rightMotor.forward();				
			}
			else if (distError > 0) {				// Too close to the wall
				
				leftMotor.setSpeed(motorStraight + 7*distError/2);	//Sharp turn before approaching very close to the wall, left motor set faster 
				rightMotor.setSpeed(motorStraight + 1*distError);	//and right motor set backwards in order to avoid collision
				leftMotor.forward();								//The speed of motors increases proportionally to the distance, it will go faster
				rightMotor.backward();								//the closer the robot is to the wall
			}
			else if (distError < 0) {				// Too far from the wall
				if(distError <= -90){				// additional filter to avoid registering very negative values  
					distError = -90; 				// for a better control for the robot's turning speed (avoids very high speeds)
				}
				leftMotor.setSpeed(motorStraight + 2*Math.abs(distError)); 	//left turn as the robot travels too far from the wall
				rightMotor.setSpeed(motorStraight + 4*Math.abs(distError));	//the speed will increase proportionally as the absolute value of the distance increases
				leftMotor.forward();
				rightMotor.forward();
			}				
	}

	@Override
	public int readUSDistance() {
		return this.distance;
	}
}