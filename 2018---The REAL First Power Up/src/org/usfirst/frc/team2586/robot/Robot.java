
/*Version 1.1 ~Last edited: 2/5/2018
 * 1.0 - First Working Build
 * 1.1 - Working Auto Code for Cross the Line and basic driving for switch control auto
*/
package org.usfirst.frc.team2586.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;


public class Robot extends IterativeRobot {
	
	
	
	//VARIABLE DECLARATIONS
	final String moveAuto = "line";
	final String switchAuto = "switch";
	String autoSelected;
	
	//enums for both auto functions
	enum switchFunc{
		forward,
		turn1,
		forward2,
		turn2,
		forward3,
		dump,
		unused
	}
	
	enum moveFunc{
		forward,
		stop,
		unused
	}
	//Naming Talons
	WPI_TalonSRX frontLeft, frontRight, rearLeft, rearRight, lift;
	WPI_VictorSPX intakeLeft, intakeRight;
	
	//Naming Joysticks
	Joystick leftStick, rightStick, XBox;
	
	//Naming mainDrive
	DifferentialDrive mainDrive;
	
	//Creating one of both enums
	switchFunc autoSwitch;
	moveFunc autoMove;
	
	//The game data received from the game
	String gameData;
	
	//declaring the shift solenoid
	Solenoid shift;
	
	//Motor ports
	//Drive Train
	final int FL = 3;
	final int RL = 4;
	final int FR = 1;
	final int RR = 2;
	//Lift, Intake L&R, Shifters
	/*final int L = 0;
	final int IL = 0;
	final int IR = 0;
	final int S = 0;
	*/
	//Joystick values
	double lX;
	double lY;
	
	double rX;
	double rY;
	//XBox joystick values
	double XBOXlY;
	double XBOXrY;
	
	double f1, t1, f2, t2, f3;
	
	Encoder leftEnc;
	Encoder rightEnc;
	
	//Sendable chooser for autonomous
	SendableChooser<String> autoChooser = new SendableChooser<>();
	public void robotInit() {
		//Initializations
		
		//declaring controllers
		XBox = new Joystick(2);
		
		leftStick = new Joystick(0);
		rightStick = new Joystick(1);
		
		//declaring encoder objects(used for tracking straight)
		leftEnc = new Encoder(1,0);
		rightEnc = new Encoder(2,3);
		
		int kDistancePerRevolution = 19;
		int kPulsesPerRevolution = 1;
		double kDistancePerPulse = kDistancePerRevolution/kPulsesPerRevolution;
		
		leftEnc.setDistancePerPulse(kDistancePerPulse);
		rightEnc.setDistancePerPulse(kDistancePerPulse);
		
		
		//declaring talon and victor objects
		frontRight = new WPI_TalonSRX(FR);
		frontLeft = new WPI_TalonSRX(FL);
		rearLeft = new WPI_TalonSRX(RL);
		rearRight = new WPI_TalonSRX(RR);	

		/*lift = new WPI_TalonSRX(L);
		intakeLeft = new WPI_VictorSPX(IL);
		intakeRight = new WPI_VictorSPX(IR);*/
		
		//Shifter
		//shift = new Solenoid(S);
		
		//declaring the drive system
		mainDrive = new DifferentialDrive(frontLeft, frontRight);
		mainDrive.setSafetyEnabled(true);
		
		//setting default and adding choices for autoChooser and adding it to smartDash
		autoChooser.addDefault("Cross the line", moveAuto);
		autoChooser.addObject("Switch Auto", switchAuto);
		SmartDashboard.putData("Auto Selection", autoChooser);
		
		//declaring slave/master for back wheels
		rearLeft.set(ControlMode.Follower, FL);
		rearRight.set(ControlMode.Follower, FR);
	}
	//BEGINNING OF AUTONOMOUS
	public void autonomousInit() {
		autoConstantSet();
		frontRight.setInverted(true);
		rearRight.setInverted(true);
		leftEnc.reset();
		rightEnc.reset();
		//get the setup of the gameboard and decide on selected auto program
		//gameData = DriverStation.getInstance().getGameSpecificMessage();
		gameData = "LRL";
		autoSelected = (String) autoChooser.getSelected();
		//Setting switch variable to first movement
		switch(autoSelected) {
		case moveAuto:
		autoMove = moveFunc.forward;
		autoSwitch = switchFunc.unused;
		break;
		case switchAuto:
		autoSwitch = switchFunc.forward;
		autoMove = moveFunc.unused;
		break;
		
		
		}
	}
	//AUTONOMOUS PERIOD
	public void autonomousPeriodic() {
		SmartDashboard.putNumber("Left Encoder Distance", leftEnc.getDistance());
		SmartDashboard.putNumber("Right Encoder Distance", rightEnc.getDistance());
	//Switch case for AUTONOMOUS SWITCH
	switch(autoSwitch) {
	case forward:
		frontLeft.set(0.2);
		frontRight.set(0.2);
		rearLeft.set(0.2);
		rearRight.set(0.2);
	if((Math.abs(leftEnc.getDistance()) + Math.abs(rightEnc.getDistance()))/2 >= f1) {
		autoSwitch = switchFunc.turn1;
		leftEnc.reset();
		rightEnc.reset();
		
	}
	//once the robot has moved forwards enough
	break;
	case turn1:
		frontLeft.set(0.2);
		rearLeft.set(0.2);
		frontRight.set(-0.2);
		rearRight.set(-0.2);
	//once the robot has turned enough
	if((Math.abs(leftEnc.getDistance()) + Math.abs(rightEnc.getDistance()))/2 >= t1) {
		autoSwitch = switchFunc.forward2;
		leftEnc.reset();
		rightEnc.reset();
	}
	break;
	case forward2:
		frontLeft.set(0.2);
		rearLeft.set(0.2);
		frontRight.set(0.2);
		rearRight.set(0.2);
	//once the robot has moved forwards enough
		if((Math.abs(leftEnc.getDistance()) + Math.abs(rightEnc.getDistance()))/2 >= f2) {
			autoSwitch = switchFunc.turn2;
			leftEnc.reset();
			rightEnc.reset();
		}
	
	break;
	case turn2:
		frontLeft.set(0.2);
		rearLeft.set(0.2);
		frontRight.set(-0.2);
		rearRight.set(-0.2);
	//once the robot has turned right enough
		if((Math.abs(leftEnc.getDistance()) + Math.abs(rightEnc.getDistance()))/2 >= t2) {
			autoSwitch = switchFunc.forward3;
			leftEnc.reset();
			rightEnc.reset();
		}
	break;
	case forward3:
		frontLeft.set(0.2);
		rearLeft.set(0.2);
		frontRight.set(0.2);
		rearRight.set(0.2);
	//once robot has moved forwards enough
		if((Math.abs(leftEnc.getDistance()) + Math.abs(rightEnc.getDistance()))/2 >= f3) {
			autoSwitch = switchFunc.dump;
			leftEnc.reset();
			rightEnc.reset();
		}
	
	break;
	case dump:
		frontLeft.set(0);
		rearLeft.set(0);
		frontRight.set(0);
		rearRight.set(0);
	break;
	case unused:
	break;
	}
	
	//Switch case for AUTONOMOUS MOVE
	switch(autoMove) {
	case forward:
	//once robot has crossed line
	frontRight.set(0.2);
	frontLeft.set(0.2);
	rearLeft.set(0.2);
	rearRight.set(0.2);
	
	if((leftEnc.getDistance() + rightEnc.getDistance())/2 >= 10000) {
		autoMove = moveFunc.stop;
	}
	break;
	
	case stop:
	frontLeft.set(0);
	frontRight.set(0);
	rearLeft.set(0);
	rearRight.set(0);
	leftEnc.reset();
	rightEnc.reset();
	frontRight.setInverted(false);
	rearRight.setInverted(false);
	break;
	
	case unused:
	break;
	}
		
	
	}
	//TELEOPERATED PERIOD
	public void teleopPeriodic() {
		//MAIN DRIVERS CODE
		
		//getting joystick variables and setting wheels to speed
		lY = leftStick.getY();
		rY = rightStick.getY();	
		lY = lY * -1;
		rY = rY * -1;
		mainDrive.tankDrive(lY, rY);
		SmartDashboard.putNumber("Left Encoder Distance", leftEnc.getDistance());
		SmartDashboard.putNumber("Right Encoder Distance", rightEnc.getDistance());
		
		//buttons for main driver
		/*if(rightStick.getRawButton(1)) {
			shift.set(true);
		}
		if(leftStick.getRawButton(1)) {
			shift.set(false);
		}*/
		
		//SECONDARY DRIVERS CODE
		
		//getting xbox values and setting motors to speed
		XBOXlY = XBox.getRawAxis(1);
		XBOXrY = XBox.getRawAxis(5);
		
		//setting lift and intake to XBox values
		//lift.set(XBOXlY);
		
		//intakeLeft.set(XBOXrY);
		//intakeRight.set(XBOXrY);
		
		//if statements for buttons on operators controller
		//if(XBox.getRawButton(1)) {
		
		//}
	}
	//TESTING PERIOD
	public void testPeriodic() {
	}
	public void autoConstantSet(){
	//	if(gameData.charAt(0) == 'L')
		if(true)
		{
			//Put left auto code here
			//set auto variables <--
			f1 = 10000;
			t1 = 10000;
			f2 = 10000;
			t2 = 10000;
			f3 = 10000;
			
		} else {
			//Put right auto code here
			//set auto variables <--
			f1 = 10000;
			t1 = 2000;
			f2 = 10000;
			t2 = 2000;
			f3 = 10000;
		}
	}
	
}
