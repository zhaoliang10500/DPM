package ca.mcgill.ecse211.lab4;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import static ca.mcgill.ecse211.lab4.Resources.*;
/**
 * @author zhaoliang & Brandon
 * This Class implemented the ultrasonic sensor localization
 */
public class USLocalizer {
  private static final int THETA_CORRECTION_FALLING_EDGE = 1; 
  private static final int THETA_CORRECTION_RISING_EDGE = -1;
  
  public float distance;
  public int isFinishedUSLocalizer = 0;
  
  //Set up here
  private Odometer odometer;
  private SampleProvider usSensor;
  private float[] usData;
  private int filterControl;
  private EV3LargeRegulatedMotor leftMotor, rightMotor;
  
  public USLocalizer (EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, 
      Odometer odometer, SampleProvider usSensor, float[] usData) {
    this.odometer = odometer;
    this.usSensor = usSensor;
    this.usData = usData;
    this.leftMotor = leftMotor;
    this.rightMotor = rightMotor;
    leftMotor.setAcceleration(ACCELERATION);
    rightMotor.setAcceleration(ACCELERATION);
    filterControl = 0;
    lastDistance = 100;
  }
  
  
  /*
   * implement the method to use ultrasonic sensor to detect the 0 degree direction
   * and make the car face to this degree
   */
  public void doLocalization(String localizationType) {
    usSensor.fetchSample(usData, 0);
    distance = (int)(usData[0]*100.0);
    
    double angle;
    leftMotor.setSpeed(ROTATION_SPEED);
    rightMotor.setSpeed(ROTATION_SPEED);
    
    /*
     * if "FALLING_EDGE" is chose, first rotate the car to the right to detect the falling edge
     * the FALL_EDGE is based on the right edge, theta is 0 when detected the right edge
     * then rotate back to detect the falling edge on the other side.
     * Recommend to use when the car is facing away from the wall
     */
    if (localizationType == "FALLING_EDGE") {
      //Detect if the vehicle is facing away or forwards the wall
      //if it is facing away from the wall, turn right unless it detects the wall
      if (filter() > DISTANCE_TO_WALL) {
        while (filter() > DISTANCE_TO_WALL) {
          leftMotor.forward();
          rightMotor.backward();
        }
        leftMotor.stop(true);
        rightMotor.stop(false);
        Sound.beep();
      }
      //if it is facing forwards the wall, turn right until it no more sees the wall
      else if (filter() < DISTANCE_TO_WALL) {
        while (filter() < DISTANCE_TO_WALL) {
          leftMotor.forward();
          rightMotor.backward();
        }
        Sound.beep();
        //rotate right a bit to make the car keep rotating
        leftMotor.rotate(180, true);
        rightMotor.rotate(-180, false);
        //Rotate the car until it sees the edge on the right, and set this angle to 0
        while (filter() > DISTANCE_TO_WALL) {
          leftMotor.forward();
          rightMotor.backward();
        }
        leftMotor.stop(true);
        rightMotor.stop(false);
        Sound.beep();
      }
      odometer.setTheta(0.0);
      
      //turning left a bit to avoid double detection
      leftMotor.rotate(-180, true);
      rightMotor.rotate(180, false);
      //Rotate the car until it sees a wall, and then Store this angle
      while (filter() > DISTANCE_TO_WALL) {
        leftMotor.backward();
        rightMotor.forward();
      }
      Sound.beep();
      angle = odometer.getXYT()[2];
      
      //stop the motors on the falling edge on the left.
      rightMotor.stop(true);
      leftMotor.stop(false);
      
      angle = 360 - angle;
      
      //half of the angle + 45 degrees to get to the 0 degree direction
      double headingToZero = angle / 2 - 45 - THETA_CORRECTION_FALLING_EDGE;
      
      leftMotor.rotate(convertAngle(WHEEL_RADIUS, WHEEL_BASE, headingToZero), true);
      rightMotor.rotate(-convertAngle(WHEEL_RADIUS, WHEEL_BASE, headingToZero), false);
      
      odometer.setTheta(0.0);
    }
    /* we implement rising edge here
     * The RISING_EDGE is based on the right edge (theta is 0 when detected the right edge)
     * Recommend to use when the car is facing forwards from the wall
     */
    else {
      //Detect if the vehicle is facing away or forwards the wall
      //if it is facing away from the wall, turn right unless it detects the wall
      if (filter() > DISTANCE_TO_WALL) {
        while (filter() > DISTANCE_TO_WALL) {
          leftMotor.forward();
          rightMotor.backward();
        }
        leftMotor.stop(true);
        rightMotor.stop(false);
        Sound.beep();
      }
      //if it is facing forwards the wall, turn right until it no more sees the wall
      else if (filter() < DISTANCE_TO_WALL) {
        while (filter() < DISTANCE_TO_WALL) {
          leftMotor.backward();
          rightMotor.forward();
        }
        Sound.beep();
      }
      odometer.setTheta(0.0);
      
      //turning left a bit to avoid double detection
      leftMotor.rotate(180, true);
      rightMotor.rotate(-180, false);
      //Rotate the car until it sees a wall, and then Store this angle
      while (filter() < DISTANCE_TO_WALL) {
        leftMotor.forward();;
        rightMotor.backward();;
      }
      Sound.beep();
      
      //store the angle
      angle = odometer.getXYT()[2];
      //stop the motors on the falling edge on the right.
      rightMotor.stop(true);
      leftMotor.stop(false);
      
      double headingToZero = angle / 2 - 135 - THETA_CORRECTION_RISING_EDGE;
      
      leftMotor.rotate(-convertAngle(WHEEL_RADIUS, WHEEL_BASE, headingToZero), true);
      rightMotor.rotate(convertAngle(WHEEL_RADIUS, WHEEL_BASE, headingToZero), false);
      
      odometer.setTheta(0.0);
    }
    isFinishedUSLocalizer = 1;
  }
  
  //Conversion methods.
  private static int convertDistance(double radius, double distance) {
      return (int) ((180.0 * distance) / (Math.PI * radius));
  }
  
  private static int convertAngle(double radius, double width, double angle) {
    return convertDistance(radius, Math.PI * width * angle / 360.0);
}
  
  /**
   * Rudimentary filter - toss out invalid samples corresponding to invalid signal.
   * returns the valid distance.
   */
  private static int FILTER_OUT = 1;
  private float lastDistance;
  
  //filter from lab2, to filter out invalid values, and return the valid value
  public float filter() {
    //get distance from the sensor
    usSensor.fetchSample(usData, 0);
    float distance = (int)(usData[0]*100.0);
    float validValue = 0;
    if (distance > 50 && filterControl < FILTER_OUT) {
        // bad value, do not set the distance var, however do increment the filter value
        filterControl++;
        validValue = lastDistance;
    } else if (distance > 50){
      validValue = 50;
    } else {
        // distance went below 255, therefore reset everything.
        filterControl = 0;
        validValue = distance;
    }
    lastDistance = distance;
    return validValue;
}
}
