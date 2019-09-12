package ca.mcgill.ecse211.lab1;

import static ca.mcgill.ecse211.lab1.Resources.*;


public class BangBangController extends UltrasonicController {

  //Define constants and variables
  private int error = 0;
  
  public BangBangController() {
    LEFT_MOTOR.setSpeed(MOTOR_HIGH); // Start robot moving forward
    RIGHT_MOTOR.setSpeed(MOTOR_HIGH);
    LEFT_MOTOR.forward();
    RIGHT_MOTOR.forward();
  }

  @Override
  public void processUSData(int distance) {
    filter(distance); //this.distance determined here
    // TODO: process a movement based on the us distance passed in (BANG-BANG style)
    error = BAND_CENTER - this.distance; // change here if we want to adjust the sensor's angle
    
    //If Vehicle is FAR AWAY
    if (error < -BAND_WIDTH) {
      if (filterControl > FILTER_OUT) {
        turnLeft();
      }
    }
    //If Vehicle is TOO CLOSE
    else if (error > BAND_WIDTH) {
        turnRight();
    }
    //If the distance is correct
    else {
        straight();
    }
    
  }
  
  public static void turnLeft() {
    LEFT_MOTOR.setSpeed(FWDSPEED-DELTASPD);
    RIGHT_MOTOR.setSpeed(FWDSPEED);
    LEFT_MOTOR.forward();
    RIGHT_MOTOR.forward();
  }
  
  public static void turnRight() {
    LEFT_MOTOR.setSpeed(FWDSPEED);
    RIGHT_MOTOR.setSpeed(FWDSPEED-DELTASPD);
    LEFT_MOTOR.forward();
    RIGHT_MOTOR.forward();
  }
  
  public static void straight() {
    LEFT_MOTOR.setSpeed(FWDSPEED);
    RIGHT_MOTOR.setSpeed(FWDSPEED);
    LEFT_MOTOR.forward();
    RIGHT_MOTOR.forward();
  }

  @Override
  public int readUSDistance() {
    return this.distance;
  }
}
