import java.util.Date;

import lejos.nxt.ColorSensor;
import lejos.nxt.LCD;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTMotor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorConstants;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.robotics.Color;
import lejos.util.Delay;


public class GameThread extends Thread {

	
	private NXTRegulatedMotor leftMotor = new NXTRegulatedMotor(MotorPort.A);
	private NXTRegulatedMotor rightMotor = new NXTRegulatedMotor(MotorPort.B);
	private NXTRegulatedMotor turnMotor = new NXTRegulatedMotor(MotorPort.C);
	private ColorSensor color = new ColorSensor(SensorPort.S1);
	
	int targetedTurnAngle = 0; //targeted turn angle. this value is not read from the engine itself. our program makes sure that motor gets this value
	int currentTacho = 0; // current known turn angle read from the motor itself.
	int maxTurnAngle = 90; //maximum angle in which car can steer
	
	int engineSpeed = 250;
	int targetedEngineSpeed = engineSpeed;
	int turboSpeed = 1500;
	
	int colorRead; //current known color
	
	long tickTime;
	long tickDiff;
	

	/**
	 * initialize the starting motor speeds/accelerations
	 */
	private void setupMotors(){
		leftMotor.setSpeed(engineSpeed);
		leftMotor.setAcceleration(780);
		
		rightMotor.setSpeed(engineSpeed);
		rightMotor.setAcceleration(780);
		
		leftMotor.backward();
		rightMotor.backward();
		
		turnMotor.setAcceleration(720);
		turnMotor.setSpeed(720);
	}
	
	/**
	 * retreives correct inner wheel speed
	 * @param speed
	 * @param angle
	 * @param maxAngle
	 * @return
	 */
	private int calculateInnerWheelSpeed(int speed, int angle, int maxAngle){

	    int maxSpeedDiff = 200;
	    	    
	    return (int) (speed - (float)((float) angle/ (float) maxAngle * (float)maxSpeedDiff));
	}
	
	/**
	 * When the car is turning the inner rear wheel should make less generations
	 * this method simulates the differential and makes sure that the inner wheel rotates correctly
	 * @param speed
	 * @param angle
	 * @param maxAngle
	 * @return int speed of the inner wheel
	 */
	private void adjustInnerWheelSpeed(){
		
		int speed = calculateInnerWheelSpeed(targetedEngineSpeed,Math.abs(currentTacho),maxTurnAngle);
		
		if (currentTacho < 0){	
			leftMotor.setSpeed(speed);
		}
		else if (currentTacho >= 0){
			rightMotor.setSpeed(speed);
		}
	}
	
	/**
	 * turns the wheels according requested turn angle
	 */
	private void performTurn(){
		if (turnMotor.getPosition() != targetedTurnAngle){
			turnMotor.rotateTo(targetedTurnAngle,true);
		}
	}
	
	/**
	 * Thread runner.
	 */
	public void run(){

		setupMotors();

		tickTime = System.currentTimeMillis();
		
		boolean rightTouch = false;
		boolean leftTouch = false;
		int rightTouchDur = 0;
		int leftTouchDur = 0;
			
		float expTurnIn = 1;
		float expTurnOut = 1;
		
		while (true){
					
			//read current tacho angle from turn motor
			currentTacho = turnMotor.getTachoCount();
			
			//read current color
			colorRead = color.getColor().getColor();
			
			tickDiff = System.currentTimeMillis() - tickTime;
			
			//every 50ms we check if the car should go left or right.
			//we dont do this in max runtime speed, to prevent overflowing the device
			if (tickDiff > 50){				
				
				tickTime = System.currentTimeMillis();
				
				if (leftTouchDur > 500 || rightTouchDur > 500){
					//targetedEngineSpeed = turboSpeed;
				}
				else{
					//targetedEngineSpeed = engineSpeed;
				}
				
				
				//adjust inner wheel speed
				adjustInnerWheelSpeed();
				 
				if (leftTouch == true){
					
					leftTouchDur += 50;
					expTurnOut = 1;
					expTurnIn += 0.1;
					
					targetedTurnAngle += expTurnIn * 2;
					targetedTurnAngle = Math.min(maxTurnAngle, targetedTurnAngle);
				}				
				else if (rightTouch == true){
					
					rightTouchDur +=50;
					expTurnOut = 1;
					expTurnIn += 0.1;
					
					targetedTurnAngle -= expTurnIn * 2;
					targetedTurnAngle = Math.max(-maxTurnAngle, targetedTurnAngle);
				}
				else{
					
					leftTouchDur = 0;
					rightTouchDur = 0;
					
					expTurnIn = 1;
					expTurnOut += 0.05;
					
					if (targetedTurnAngle < 0){
						targetedTurnAngle += expTurnOut * 2;
						targetedTurnAngle = Math.min(0, targetedTurnAngle);
					}
					else if (targetedTurnAngle > 0){
						targetedTurnAngle -= expTurnOut * 2;
						targetedTurnAngle = Math.max(0, targetedTurnAngle);
					}
				}				
			}
			
			//check in maximum speed the current color. 
			//based on the booleans set we perform certain actions in above 50ms capped procedure
			switch (colorRead){
				case lejos.robotics.Color.GREEN:
					rightTouch = false;
					leftTouch = true;	
				break;
				case lejos.robotics.Color.BLUE:
					leftTouch = false;
					rightTouch = true;
				break;
				case lejos.robotics.Color.RED:
					//TODO
				break;	
				default:
					leftTouch = false;
					rightTouch = false;
					break;
			}
			
			//perform turn actions
			performTurn();
		}
	}
	
}
