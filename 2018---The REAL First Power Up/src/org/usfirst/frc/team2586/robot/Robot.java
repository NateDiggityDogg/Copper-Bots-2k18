/**
 * Version 1.2 2/19/2018 ~Added lift functionality but haven't bug tested, also commented code better and broke it to easier
 * to manage chunks
 */

package org.usfirst.frc.team2586.robot;

import com.analog.adis16448.frc.ADIS16448_IMU;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.interfaces.Gyro;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends IterativeRobot {

	// VARIABLE DECLARATIONS
	final String moveAuto = "line";
	final String switchAuto = "switch";
	String autoSelected;

	String gear = "";
	boolean retracted = true;
	boolean clamped;
	Compressor comp;
	DoubleSolenoid shifter;
	DoubleSolenoid clamp;
	DoubleSolenoid retract;

	PID driveLeftPID, driveRightPID, liftPID;

	// enums for both auto functions
	enum switchFunc {
		forward, turn1, forward2, turn2, forward3, dump, unused
	}

	enum moveFunc {
		forward, stop, unused
	}

	// Naming Talons
	WPI_TalonSRX frontLeft, frontRight, rearLeft, rearRight, lift;
	WPI_VictorSPX intakeLeft, intakeRight;
	
	//Limit Switches
	DigitalInput topLimit, botLimit;

	// Naming Joysticks
	Joystick leftStick, rightStick, XBox;

	// Naming mainDrive
	DifferentialDrive mainDrive;

	// Creating one of both enums
	switchFunc autoSwitch;
	moveFunc autoMove;

	// The game data received from the game
	String gameData;

	// direction boolean for deciding switch control direction
	boolean left;
	boolean gyroMode = false;
	double gyroHeading = 0;

	String presetSwitch = "Left";

	// Motor ports
	// Drive Train
	final int FL = 3;
	final int RL = 4;
	final int FR = 1;
	final int RR = 2;
	// Lift, Intake L&R, Shifters
	
	final int L = 0; 
	final int IL = 0; 
	final int IR = 0;
	 
	// Joystick values
	double lX;
	double lY;

	double rX;
	double rY;
	
	double lT;
	double rT;
	// XBox joystick values
	double XBOXlY;
	double XBOXrY;

	double f1, t1, f2, t2, f3;

	double autoSpeed;

	private CameraServer camera;

	// gyro
	ADIS16448_IMU gyro;

	// encoders
	Encoder leftEnc;
	Encoder rightEnc;
	Encoder liftEnc;

	// Sendable chooser for autonomous
	SendableChooser<String> autoChooser = new SendableChooser<>();
	SendableChooser autoRate = new SendableChooser<>();

	/**
	 * -------------------------------------------------------------------------------------------------------------------------------
	 * ROBOT INITIALIZATION
	 */
	public void robotInit() {

		//Pneumatics
		comp = new Compressor();
		comp.start();

		shifter = new DoubleSolenoid(0, 1);
		clamp = new DoubleSolenoid(2, 3);
		retract = new DoubleSolenoid(4,5);

		// Controllers
		XBox = new Joystick(2);

		leftStick = new Joystick(0);
		rightStick = new Joystick(1);
		
		//Encoders
		leftEnc = new Encoder(0, 1);
		rightEnc = new Encoder(2, 3);
		liftEnc = new Encoder(4, 5);

		//Gyro
		gyro = new ADIS16448_IMU();
		gyro.calibrate();

		//Drivebase
		frontRight = new WPI_TalonSRX(FR);
		frontLeft = new WPI_TalonSRX(FL);
		rearLeft = new WPI_TalonSRX(RL);
		rearRight = new WPI_TalonSRX(RR);

		// declaring slave/master for back wheels
		rearLeft.set(ControlMode.Follower, FL);
		rearRight.set(ControlMode.Follower, FR);

		//Lift System
		lift = new WPI_TalonSRX(L);
		intakeLeft = new WPI_VictorSPX(IL);
		intakeRight = new WPI_VictorSPX(IR);
		
		topLimit = new DigitalInput(0);
		botLimit = new DigitalInput(0);


		// declaring the drive system
		mainDrive = new DifferentialDrive(frontLeft, frontRight);
		mainDrive.setSafetyEnabled(true);

		//Set PIDs
		/*
		driveLeftPID = new PID(0.002586, 0, 0);
		driveRightPID = new PID(0.002586, 0, 0);
		*/
		driveLeftPID = new PID(0.00005, 0, 0);
		driveRightPID = new PID(0.00005, 0, 0);
		
		liftPID = new PID(0.002586, 0, 0);

		// setting default and adding choices for autoChooser and adding it to smartDash
		autoChooser.addDefault("Cross the line", moveAuto);
		autoChooser.addObject("Switch Auto", switchAuto);
		
		SmartDashboard.putData("Auto Selection", autoChooser);
		
//		autoSpeed = SmartDashboard.getNumber("Auto Speed", 0.2);
		
		SmartDashboard.putNumber("speed", autoSpeed);
		
		//Live Stream Camera
		// CameraServer camera = CameraServer.getInstance();
		// camera.addServer("Front Cam");
		// camera.startAutomaticCapture();
	}

	/**
	 * ------------------------------------------------------------------------------------------------------------------------------
	 * BEGINNING OF AUTONOMOUS PHASE
	 */
	public void autonomousInit() {

		autoConstantSet();
		leftEnc.reset();
		rightEnc.reset();
		// get the setup of the gameboard and decide on selected auto program
		gameData = DriverStation.getInstance().getGameSpecificMessage();
		
		autoSelected = (String) autoChooser.getSelected();
		// Setting switch variable to first movement
		switch (autoSelected) {
		case moveAuto:
			autoMove = moveFunc.forward;
			//Robot throws an error without this random unused enum being declared, I'm assuming its because its trying to switch an undeclared variable?
			autoSwitch = switchFunc.unused;
			break;
		case switchAuto:
			autoSwitch = switchFunc.forward;
			autoMove = moveFunc.unused;
			break;

		}
	}

	// AUTONOMOUS PERIOD
	public void autonomousPeriodic() {
		SmartDashboard.putNumber("Left Encoder Distance", leftEnc.getDistance());
		SmartDashboard.putNumber("Right Encoder Distance", rightEnc.getDistance());
		gyroHeading = gyro.getAngle();
		// Switch case for AUTONOMOUS SWITCH
		switch (autoSwitch) {
			case forward:
				// forwards(autoSpeed);
				if ((Math.abs(leftEnc.getDistance()) + Math.abs(rightEnc.getDistance())) / 2 >= f1) {
					autoSwitch = switchFunc.turn1;
					reset();
	
				}
	
				// once the robot has moved forwards enough
				break;
			case turn1:
				if (left) {
					// turnLeft(autoSpeed);
				} else {
					// turnRight(autoSpeed);
				}
				// once the robot has turned enough
				if ((Math.abs(leftEnc.getDistance()) + Math.abs(rightEnc.getDistance())) / 2 >= t1) {
					autoSwitch = switchFunc.forward2;
					reset();
				}
				break;
	
			case forward2:
			//	forwards(autoSpeed);
				// once the robot has moved forwards enough
				if ((Math.abs(leftEnc.getDistance()) + Math.abs(rightEnc.getDistance())) / 2 >= f2) {
					autoSwitch = switchFunc.turn2;
					leftEnc.reset();
					rightEnc.reset();
				}
	
				break;
			case turn2:
				if (left) {
					// turnRight(autoSpeed);
				} else {
					// turnLeft(autoSpeed);
				}
				// once the robot has turned right enough
				if ((Math.abs(leftEnc.getDistance()) + Math.abs(rightEnc.getDistance())) / 2 >= t2) {
					autoSwitch = switchFunc.forward3;
					leftEnc.reset();
					rightEnc.reset();
				}
				break;
			case forward3:
				// forwards(autoSpeed);
				// once robot has moved forwards enough
				if ((Math.abs(leftEnc.getDistance()) + Math.abs(rightEnc.getDistance())) / 2 >= f3) {
					autoSwitch = switchFunc.dump;
					leftEnc.reset();
					rightEnc.reset();
				}
	
				break;
			case dump:
				// forwards(0);
				break;
			case unused:
				break;
		}

		// Switch case for AUTONOMOUS MOVE
		switch (autoMove) {
		case forward:
		
			frontLeft.set(driveLeftPID.getOutput(Math.abs(leftEnc.getDistance()), Constants.autoDriveLeftTar));
			frontRight.set(-driveRightPID.getOutput(Math.abs(rightEnc.getDistance()), Constants.autoDriveRightTar));
			
			smartDash();
			
			if ((Math.abs(leftEnc.getDistance()) + Math.abs(rightEnc.getDistance())) / 2 >= 3000) {
				autoMove = moveFunc.stop;
			}
			break;

		case stop:
		//	forwards(0);
			frontLeft.set(0);
			frontRight.set(0);
			reset();
			break;

		case unused:
			break;
		}

	}

	/**
	 * -------------------------------------------------------------------------------------------------------------------------
	 * BEGINNING OF TELEOP PHASE
	 */
	@Override
	public void teleopInit() {
		leftEnc.reset();
		rightEnc.reset();
	}

	/**
	 * This is the teleop phase, essentially an infinite "while" loop thats run during teleop phase,
	 * it calls on the two controller classes
	 */
	
	@Override
	public void teleopPeriodic() {
		driveBase();
		lift();
	}

	/**
	 * Binding the joysticks for the main driver, they're currently on XBox controller b/c of logistics reasons
	 */
	
	public void driveBase() {
		smartDash();
		// MAIN DRIVERS CODE
		// getting joystick variables and setting wheels to speed
		
		// Arcade Drive w/ XBOX Controller
		lY = XBox.getRawAxis(1);
		rY = XBox.getRawAxis(4);
		
		mainDrive.arcadeDrive(-lY, rY);

		// Tank Drive w/ Joysticks
		// lY = leftStick.getY();
		// rY = rightStick.getY();

		// mainDrive.tankDrive(lY, rY);
	
	}

	/**
	 * This is the lift function used in the teleop phase, it binds the operators controller
	 */
	
	public void lift() {
		lT = XBox.getRawAxis(2);
		rT = XBox.getRawAxis(3);
		lY = XBox.getRawAxis(1) * -1;
		lift.set(lY);
		
		//Shifting to low w/ A Button
		if(XBox.getRawButton(1)){
			shifter.set(Value.kForward);
			gear = "Low";
		}
		
		//Shifting to high w/ Y Button
		if(XBox.getRawButton(4)) {
			shifter.set(Value.kReverse);
			gear = "High";
		}
		
		//Clamping together w/ Left Bumper
		if (XBox.getRawButton(5)) {
			clamp.set(Value.kReverse);
			clamped = true;
		}
		
		//Unclamping w/ Right Bumper
		if (XBox.getRawButton(6)) {
			clamp.set(Value.kForward);
			clamped = false;
		}
		
		//Sending out arm w/ Strt Button
		if(XBox.getRawButton(8)) {
		retract.set(Value.kForward);
		retracted = false;
		}
		
		//Intake with Left Trigger
		if (lT > rT && lT > 0.1) {
			intakeLeft.set(lT);
			intakeRight.set(lT);
		}
		
		//Send Out with Right Trigger
		if (rT > lT && rT > 0.1) {
			intakeLeft.set(-rT);
			intakeRight.set(-rT);
		}
		
		//Stop wheels if trigs aren't used
		if(lT < 0.1 && rT < 0.1) {
			intakeLeft.set(0);
			intakeRight.set(0);
		}
		
		//Top Limit Switch sets liftspeed to 0
		if(topLimit.get()) {
			lift.set(0);
		}
		
		//Bot Limit Switch sets liftspeed to 0 and resets encoder
		if (botLimit.get()) {
			lift.set(0);
			liftEnc.reset();
		}
		
	}

	// TESTING PERIOD
	public void testPeriodic() {
	}
	
	/**
	 * -------------------------------------------------------------------------------------------------------------------------------
	 * Custom Functions
	 */
	
	public void smartDash() {
		SmartDashboard.putNumber("lY", lY);
		SmartDashboard.putNumber("rY", rY);

		SmartDashboard.putNumber("Left Encoder Distance", leftEnc.getDistance());
		SmartDashboard.putNumber("Right Encoder Distance", rightEnc.getDistance());

		SmartDashboard.putNumber("Gyro Angle", gyro.getAngleZ());
		SmartDashboard.putString("Gear", gear);
		SmartDashboard.putBoolean("Clamped", clamped);
		SmartDashboard.putBoolean("Retracted", retracted);
	}

	public void autoConstantSet() {
		// if(gameData.charAt(0) == 'L')
		if (presetSwitch.charAt(0) == 'L') {
			// Put left auto code here
			// set auto variables <--
			f1 = 10000;
			t1 = 10000;
			f2 = 10000;
			t2 = 10000;
			f3 = 10000;
			left = true;

		} else {
			// Put right auto code here
			// set auto variables <--
			f1 = 10000;
			t1 = 10000;
			f2 = 10000;
			t2 = 10000;
			f3 = 10000;
			left = false;
		}
	}

	public void reset() {
		leftEnc.reset();
		rightEnc.reset();
	}

}
