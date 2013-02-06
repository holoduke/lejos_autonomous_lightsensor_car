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

	
	private NXTRegulatedMotor motorA = new NXTRegulatedMotor(MotorPort.A);
	private NXTRegulatedMotor motorB = new NXTRegulatedMotor(MotorPort.B);
	private NXTRegulatedMotor motorC = new NXTRegulatedMotor(MotorPort.C);
	
	public static void main(String[] args) {
	}
	
	public String getColorString(int color){
		switch (color){
		case lejos.robotics.Color.BLACK:
			return "Black";
		case lejos.robotics.Color.YELLOW:
			return "Yellow";
		case lejos.robotics.Color.RED:
			return "Red";
		}
		
		return "color not dedected "+color;
	}
	
	public int getSpeed(int speed, int angle, int maxAngle){

	    int maxSpeedDiff = 200;
	    	    
	    return (int) (speed - (float)((float) angle/ (float) maxAngle * (float)maxSpeedDiff));
	}

	public void run(){
		int i = 0;

		ColorSensor color = new ColorSensor(SensorPort.S1);

		int engineSpeed = 250;
		int targetedEngineSpeed = engineSpeed;
		int turboSpeed = 1500;
		int maxTurnAngle = 90;
		
		motorA.setSpeed(engineSpeed);
		motorA.setAcceleration(780);
		
		motorB.setSpeed(engineSpeed);
		motorB.setAcceleration(780);
		
		motorA.backward();
		motorB.backward();
		
		motorC.setAcceleration(720);
		motorC.setSpeed(720);
		//motorC.rotate(100);
		//motorC.rotate(-100);
		
		long ms2 = System.currentTimeMillis();
		
		boolean rightTouch = false;
		boolean leftTouch = false;
		int rightTouchDur = 0;
		int leftTouchDur = 0;
		
		int angle = 0;
		
		float expTurnIn = 1;
		float expTurnOut = 1;
		
		while (true){
					
			i++;
			int currentTacho = motorC.getTachoCount();
			int colorRead = color.getColor().getColor();
			
			long tick = System.currentTimeMillis() - ms2;
						
			if (tick > 50){				
				
				if (i==50){
					Sound.playTone(2349,250,1000);
					i =0;
				}
								
				ms2 = System.currentTimeMillis();
				
				
				if (leftTouchDur > 500 || rightTouchDur > 500){
					//targetedEngineSpeed = turboSpeed;
				}
				else{
					//targetedEngineSpeed = engineSpeed;
				}
				
				
				int speed = getSpeed(targetedEngineSpeed,Math.abs(currentTacho),maxTurnAngle);
				
				if (currentTacho < 0){	
					motorA.setSpeed(speed);
				}
				else if (currentTacho >= 0){
					motorB.setSpeed(speed);
				}
			
				if (leftTouch == true){
					
					leftTouchDur += 50;
					expTurnOut = 1;
					expTurnIn += 0.6;
					
					angle += expTurnIn * 1;
					angle = Math.min(maxTurnAngle, angle);
		
				}				
				else if (rightTouch == true){
//					
					rightTouchDur +=50;
					expTurnOut = 1;
					expTurnIn += 0.6;
					
					angle -= expTurnIn * 1;
					angle = Math.max(-maxTurnAngle, angle);
					
					LCD.drawString("rt: "+angle, 0, 0);

				}
				else{
					
					leftTouchDur = 0;
					rightTouchDur = 0;
					
					expTurnIn = 1;
					expTurnOut += 0.6;
					
					if (angle < 0){
						angle += expTurnOut;
						angle = Math.min(0, angle);
					}
					else if (angle > 0){
						angle -= expTurnOut;
						angle = Math.max(0, angle);
					}
				}				
			}
			
			
			
			switch (colorRead){
			case lejos.robotics.Color.YELLOW:
				
				rightTouch = false;
				
				LCD.drawString("green: "+angle, 0, 0);
				//motorC.rotateTo(angle,true);
			
				leftTouch = true;	
			
			break;
			case lejos.robotics.Color.BLUE:
				
				leftTouch = false;
				LCD.drawString("blue: "+angle, 0, 0);
				//motorC.rotateTo(angle,true);
				
				rightTouch = true;

			break;
			case lejos.robotics.Color.RED:
				//motorA.setSpeed(50);
				//motorB.setSpeed(50);
				break;	
			default:
				LCD.drawString("none: "+angle, 0, 0);
				//LCD.drawString("straight", 0, 0);
				//motorC.rotateTo(angle,true);
				
				leftTouch = false;
				rightTouch = false;
	
				break;
			}
			
			
			if (motorC.getPosition() != angle){
				motorC.rotateTo(angle,true);
			}
			
			
//			Delay.msDelay(500);
		}
	}
	
	public void Steer(int Angle){
		int maxAngle = 100;
		
		
		
	}
	
	
}
