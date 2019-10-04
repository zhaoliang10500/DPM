package ca.mcgill.ecse211.lab3;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import static ca.mcgill.ecse211.lab3.Resources.*;

/**
 * @author zhaoliang & Brandon
 * This Class implemented the Avoidance and Simple navigaton functions
 */
public class Main {
  
  //4 waypoints pattern to choose
  private static final int[][] waypoints1 = {{1,3},{2,2},{3,3},{3,2},{2,1}};
  private static final int[][] waypoints2 = {{2,2},{1,3},{3,3},{3,2},{2,1}};
  private static final int[][] waypoints3 = {{2,1},{3,2},{3,3},{1,3},{2,2}};
  private static final int[][] waypoints4 = {{1,2},{2,3},{2,1},{3,2},{3,3}};
  
  //define variables
  private static boolean navigating = true;
  static int distance;
  
  //Setting up the sensor
  @SuppressWarnings("resource")
  static SensorModes usSensor = new EV3UltrasonicSensor(Resources.usPort);       
  static SampleProvider usDistance = usSensor.getMode("Distance");   
  static float[] usData = new float[usDistance.sampleSize()];
  
  //Setting odometer and display as two threads
  static Odometer odometer = new Odometer(Resources.leftMotor, Resources.rightMotor);
  static Display display = new Display(odometer);
  
  public static void main(String[] args) {
    //Display on the screen defines here
    int buttonChoice;
        
    do {
      LCD.clear();
      
      LCD.drawString("< Left | Right >", 0, 0);
      LCD.drawString("       |        ", 0, 1);
      LCD.drawString(" do    | nav    ", 0, 2);
      LCD.drawString(" simple| avoid  ", 0, 3);
      LCD.drawString(" nav   | nav    ", 0, 4);
      buttonChoice = Button.waitForAnyPress();
    } while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);
    
    if (buttonChoice == Button.ID_LEFT){
        odometer.start();
        display.start();
        Navigation(waypoints1);
        
    } else {
        odometer.start();
        display.start();
        Avoidance(waypoints1);
    }
    
    while (Button.waitForAnyPress() != Button.ID_ESCAPE);
    System.exit(0);
    }
  
  //****Avoidance mode defined here*****
  private static void Avoidance (int[][] waypoints) {
    for (int i = 0; i < 5; i++) {
      double x = Resources.TILE_LENGTH * waypoints2[i][0];
      double y = Resources.TILE_LENGTH * waypoints2[i][1];
      advancedTravelTo(x, y);
    }
  }
  
  //****Simple navigation mode defined here
  private static void Navigation (int[][] waypoints)  {
    for (int i = 0; i < 5; i++) {
      double x = Resources.TILE_LENGTH * waypoints[i][0];
      double y = Resources.TILE_LENGTH * waypoints[i][1];
      normalTravelTo(x, y);
    }
}
  
  
  
  
//***** methods used in navigation and avoidance modes are defined below*******
  /**
   * @param double angle representing the angle heading change in radians 
   * @return minimum degrees needed to turn to the required angle
   */
  public static double getMinAngle(double angle) {
    if (angle > Math.PI) {
      angle = 2 * Math.PI - angle;
    }
    else if (angle < -Math.PI) {
      angle = angle + 2 * Math.PI;
    }
    return angle;
  }
  
  
  /** parameters: double theta that represents an angle in radians
   *  action: changes direction from current angle to theta
   */
  public static void turnTo(double theta) {
      double angle = theta- odometer.getTheta();
      
      leftMotor.rotate(changeToDesiredAngle(getMinAngle(angle)),true);
      rightMotor.rotate(-changeToDesiredAngle(getMinAngle(angle)),false);
  }
  
  
  /** parameter: double distance indicates how far the car should run
   *  returns: degress the wheels have to turn to get to the next waypoint
   */
  private static int travelToNextWaypoint(double distance){
      return (int) (180*distance/(PI * Resources.WHEEL_RADIUS));
  }
  
  
  /** parameter: double angle representing the angle heading change in radians
   *  returns: degrees the motors have to turn to change this direction
   */
  private static int changeToDesiredAngle(double angle){
      return travelToNextWaypoint(Resources.WHEEL_BASE*angle/2);
  }

  
  /**
   *  returns: whether the vehicle is currently navigating
   */
  public static boolean isNavigating() {
      return navigating;
  }
  
  
  //***implement bang-bang controller here to avoid obsticle****
  public static void avoidObstacle(){
    turnTo(odometer.getTheta()-PI/2);
    // adjust the robot heading to ensure the avoidance of obstacles
    sensorMotor.rotateTo(Resources.SENSOR_ANGLE);
    
    // define the exit condition of avoidance mode
    //we found 0.8PI works best for assuming the car has passed the obstacle
    //and is able to restart on its previous trajectory angle
    double exitAngleForAvoidanceMode = odometer.getTheta()+ PI * 0.8;
    while (odometer.getTheta() < exitAngleForAvoidanceMode){
      //get data from the Sample
      usDistance.fetchSample(usData,0);                           
      distance=(int)(usData[0]*100.0);
      //Use bang-bang Controller to avoid obstacle
      int errorDistance = Resources.bandCenter - distance;
      if (Math.abs(errorDistance)<= Resources.bandWidth){ //moving in straight line
          leftMotor.setSpeed(Resources.FWDSPEED_TO_OBSTACLE);
          rightMotor.setSpeed(Resources.FWDSPEED_TO_OBSTACLE);
          leftMotor.forward();
          rightMotor.forward();
      } else if (errorDistance > 0){ //too close to wall
          leftMotor.setSpeed(Resources.OBSTACLE_TURN_OUT_SPEED * 2/5);// Setting the outer wheel to reverse
          rightMotor.setSpeed(Resources.FWDSPEED_TO_OBSTACLE * 2/5); 
          leftMotor.backward();
          rightMotor.forward();
      } else if (errorDistance < 0){ // getting too far from the wall
          rightMotor.setSpeed(Resources.FWDSPEED_TO_OBSTACLE * 2/5);
          leftMotor.setSpeed(Resources.OBSTACLE_TURN_IN_SPEED * 2/5);// Setting the outer wheel to move faster
          rightMotor.forward();
          leftMotor.forward();
      }
    }
    Sound.beep();
    leftMotor.stop();
    rightMotor.stop();
    
}
  
  //**** advancedTravel mode used in Avoidance**********
  public static void advancedTravelTo(double x, double y) {
    //reset motors
    leftMotor.stop();
    rightMotor.stop();
    leftMotor.setAcceleration(1000);
    rightMotor.setAcceleration(1000);
    
    //calculate angle we need to turn in order to get the next waypoint
    double trajectoryX = x - odometer.getX() - Resources.TILE_LENGTH;
    double trajectoryY = y - odometer.getY() - Resources.TILE_LENGTH;
    double trajectoryAngle = Math.atan2(trajectoryX, trajectoryY);
    
    //rotate to required angle
    Sound.beep();
    leftMotor.setSpeed(Resources.ROTATE_SPEED);
    rightMotor.setSpeed(Resources.ROTATE_SPEED);
    turnTo(trajectoryAngle);
    
    //calculate distance we need to move in order to get the next waypoint
    double trajectoryLine = Math.hypot(trajectoryX, trajectoryY);
    Sound.beep();
    leftMotor.setSpeed(Resources.FORWARD_SPEED);
    rightMotor.setSpeed(Resources.FORWARD_SPEED);
    leftMotor.rotate(travelToNextWaypoint(trajectoryLine),true);
    rightMotor.rotate(travelToNextWaypoint(trajectoryLine),true);
    
    //make the ultronsonic sensor turn
    int distance;
    sensorMotor.resetTachoCount();
    sensorMotor.setSpeed(Resources.SCAN_SPEED);
    
    //Scan the surrounding when the robot is moving
    while (leftMotor.isMoving() || rightMotor.isMoving()) {
      //Rotate the sensor if it's not already rotating
      while (!sensorMotor.isMoving()){
      //critical angel is the buffer degree for the sensor to decide turning right or left
      if (sensorMotor.getTachoCount() >= Resources.CRITICAL_ANGLE){
          sensorMotor.rotateTo(Resources.LEFT_ANGLE,true);
      } else {
          sensorMotor.rotateTo(Resources.RIGHT_ANGLE,true);
      }
      }
      //store the data to sample and set it to distance.
      usDistance.fetchSample(usData,0);                          
      distance=(int)(usData[0]*100.0);
      filter(distance);
      
      if(distance <= Resources.bandCenter){
          Sound.beep();
          leftMotor.stop(true); // Stop the robot and quit navigation mode
          rightMotor.stop(false);
          navigating = false;
      }
      try { Thread.sleep(50); } catch(Exception e){}
    
  }
      
      if (!isNavigating()){
          avoidObstacle(); // use bangbang controller to avoid the obstacle
          sensorMotor.rotateTo(0); // reset sensor position
          navigating = true; // re-enable navigation mode
          advancedTravelTo(x,y); // continue traveling to destination
          return;
      }
      sensorMotor.rotateTo(0);
      
  }
  
  
  //****Normal Travel mode used in Simple navigation******
  public static void normalTravelTo(double x, double y) {
      
    //reset motors
    leftMotor.stop();
    rightMotor.stop();
    leftMotor.setAcceleration(1000);
    rightMotor.setAcceleration(1000);
    
    //calculate trajectory path and angle
    double trajectoryX = x - odometer.getX() - Resources.TILE_LENGTH;
    double trajectoryY = y - odometer.getY() - Resources.TILE_LENGTH;
    double trajectoryAngle = Math.atan2(trajectoryX, trajectoryY);
    
    //rotate to correct angle
    Sound.beepSequenceUp();
    leftMotor.setSpeed(Resources.ROTATE_SPEED);
    rightMotor.setSpeed(Resources.ROTATE_SPEED);
    turnTo(trajectoryAngle);
    
    double trajectoryLine = Math.hypot(trajectoryX, trajectoryY);
    
    //move forward correct distance
    Sound.beepSequence();
    leftMotor.setSpeed(Resources.FORWARD_SPEED);
    rightMotor.setSpeed(Resources.FORWARD_SPEED);
    leftMotor.rotate(travelToNextWaypoint(trajectoryLine),true);
    rightMotor.rotate(travelToNextWaypoint(trajectoryLine),false);
  }
  
//Filtering out bad results
 public static void filter(int distance) {
   int FILTER_OUT = 25;
   int filterControl = 0;
   if (distance >= 255 && filterControl < FILTER_OUT) {
       // bad value, do not set the distance var, however do increment the
       // filter value
       filterControl++;
   } else if (distance >= 255) {
       // We have repeated large values, so there must actually be nothing
       // there: leave the distance alone
       distance = distance;
   } else {
       // distance went below 255: reset filter and leave
       // distance alone.
       filterControl = 0;
       distance = distance;
   }
 }
}

