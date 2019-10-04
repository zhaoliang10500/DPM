package ca.mcgill.ecse211.lab3;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;

public class Resources {
  //Initialize class variables
  public static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
  public static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
  public static final EV3LargeRegulatedMotor sensorMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));
  public static final Port usPort = LocalEV3.get().getPort("S2");
  
  //constants
  public static final double WHEEL_RADIUS = 2.13;
  public static final double WHEEL_BASE = 10.8;
  public static final int bandCenter = 10;
  public static final int bandWidth = 1;
  public static final int FORWARD_SPEED = 150;
  public static final int ROTATE_SPEED = 100;
  public static final double TILE_LENGTH = 30.48;
  public static final double PI = Math.PI;
  
  //constants for Avoidance
  public static final int SCAN_SPEED = 250;
  public static final int RIGHT_ANGLE = 55;
  public static final int LEFT_ANGLE = -55;
  public static final int CRITICAL_ANGLE = 10;
  public static final int SENSOR_ANGLE = 80;
  public static final int FWDSPEED_TO_OBSTACLE = 150;
  public static final int OBSTACLE_TURN_IN_SPEED = 275;
  public static final int OBSTACLE_TURN_OUT_SPEED = 60;
  
  public static final TextLCD LCD = LocalEV3.get().getTextLCD();
}
