package ca.mcgill.ecse211.lab1;

import static ca.mcgill.ecse211.lab1.Resources.*;

public class PController extends UltrasonicController {

  private static final int MOTOR_SPEED = 200;
  private final int SCALING_FACTOR = 10;
  private final int MIN_SPEED = 150;
  private final int MAX_SPEED = 350;
  
  private int error = 0;

  public PController() {
    LEFT_MOTOR.setSpeed(MOTOR_SPEED); // Initialize motor rolling forward
    RIGHT_MOTOR.setSpeed(MOTOR_SPEED);
    LEFT_MOTOR.forward();
    RIGHT_MOTOR.forward();
  }

  @Override
  public void processUSData(int distance) {
    filter(distance);
    // TODO: process a movement based on the us distance passed in (P style)
    error = BAND_CENTER - this.distance;
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

  private void turnLeft() {
    LEFT_MOTOR.setSpeed(Math.max((FWDSPEED - SCALING_FACTOR * error), MIN_SPEED));
    RIGHT_MOTOR.setSpeed(Math.min((FWDSPEED + SCALING_FACTOR * error), MAX_SPEED));
    LEFT_MOTOR.forward();
    RIGHT_MOTOR.forward();
  }
  
  private void turnRight() {
    LEFT_MOTOR.setSpeed(Math.min((FWDSPEED + SCALING_FACTOR * error), MAX_SPEED));
    RIGHT_MOTOR.setSpeed(Math.max((FWDSPEED - SCALING_FACTOR * error), MIN_SPEED));
    LEFT_MOTOR.forward();
    RIGHT_MOTOR.forward();
  }
  
  private void straight() {
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
