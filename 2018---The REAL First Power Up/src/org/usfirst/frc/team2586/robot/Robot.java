/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */

/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

//Version 1.0 ~Last edited: 1/29/2018

package org.usfirst.frc.team2586.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Solenoid;
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
		dump
	}
	
	enum moveFunc{
		forward,
		stop
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
	final int FL = 0;
	final int RL = 0;
	final int FR = 0;
	final int RR = 0;
	//Lift, Intake L&R, Shifters
	final int L = 0;
	final int IL = 0;
	final int IR = 0;
	final int S = 0;
	
	//Joystick values
	double lX;
	double lY;
	
	double rX;
	double rY;
	//XBox joystick values
	double XBOXlY;
	double XBOXrY;
	
	Encoder leftDrive;
	Encoder rightDrive;
	
	//Sendable chooser for autonomous
	SendableChooser<String> autoChooser = new SendableChooser<>();
	public void robotInit() {
		//Initializations
		
		//declaring controllers
		XBox = new Joystick(2);
		
		leftStick = new Joystick(0);
		rightStick = new Joystick(1);
		
		//declaring encoder objects(used for tracking straight)
		leftDrive = new Encoder(0,0);
		rightDrive = new Encoder(0,0);
		
		//declaring talon and victor objects
		frontRight = new WPI_TalonSRX(FR);
		frontLeft = new WPI_TalonSRX(FL);
		rearLeft = new WPI_TalonSRX(RL);
		rearRight = new WPI_TalonSRX(RR);	
		lift = new WPI_TalonSRX(L);
		intakeLeft = new WPI_VictorSPX(IL);
		intakeRight = new WPI_VictorSPX(IR);
		
		//Shifter
		shift = new Solenoid(S);
		
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
		//get the setup of the gameboard and decide on selected auto program
		gameData = DriverStation.getInstance().getGameSpecificMessage();
		autoSelected = (String) autoChooser.getSelected();
		//Setting switch variable to first movement
		switch(autoSelected) {
		case moveAuto:
		autoMove = moveFunc.forward;	
		break;
		case switchAuto:
		autoConstantSet();
		break;
		
		
		}
	}
	//AUTONOMOUS PERIOD
	public void autonomousPeriodic() {
	//Switch case for AUTONOMOUS SWITCH
	switch(autoSwitch) {
	case forward:
	frontLeft.set(0);
	frontRight.set(0);
	if((leftDrive.getDistance() + rightDrive.getDistance())/2 >= 1000) {
		frontLeft.set(0);
		frontRight.set(0);
		autoSwitch = switchFunc.turn1;
	}
	//once the robot has moved forwards enough
	break;
	case turn1:
	//once the robot has turned enough
	autoSwitch = switchFunc.forward2;
	break;
	case forward2:
	//once the robot has moved forwards enough
	autoSwitch = switchFunc.turn2;
	break;
	case turn2:
	//once the robot has turned right enough
	autoSwitch = switchFunc.forward3;
	break;
	case forward3:
	//once robot has moved forwards enough
	autoSwitch = switchFunc.dump;
	break;
	case dump:
	break;
	}
	//Switch case for AUTONOMOUS MOVE
	switch(autoMove) {
	case forward:
	//once robot has crossed line
	autoMove = moveFunc.stop;
	break;
	case stop:
	break;
	}
		
		
	}
	//TELEOPERATED PERIOD
	public void teleopPeriodic() {
		//MAIN DRIVERS CODE
		
		//getting joystick variables and setting wheels to speed
		lY = leftStick.getY();
		rY = rightStick.getY();		
		mainDrive.tankDrive(lY, rY);
		
		//buttons for main driver
		if(rightStick.getRawButton(1)) {
			shift.set(true);
		}
		if(leftStick.getRawButton(1)) {
			shift.set(false);
		}
		
		//SECONDARY DRIVERS CODE
		
		//getting xbox values and setting motors to speed
		XBOXlY = XBox.getRawAxis(1);
		XBOXrY = XBox.getRawAxis(5);
		
		//setting lift and intake to XBox values
		lift.set(XBOXlY);
		
		intakeLeft.set(XBOXrY);
		intakeRight.set(XBOXrY);
		
		//if statements for buttons on operators controller
		if(XBox.getRawButton(1)) {
		
		}
	}
	//TESTING PERIOD
	public void testPeriodic() {
	}
	public void autoConstantSet(){
		if(gameData.charAt(0) == 'L')
		{
			//Put left auto code here
			//set auto variables <--
			autoSwitch = switchFunc.forward;
			
		} else {
			//Put right auto code here
			//set auto variables <--
			autoSwitch = switchFunc.forward;
		}
	}
	
}
