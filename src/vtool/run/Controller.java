package vtool.run;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javafx.scene.control.ComboBox;

public class Controller {

  // Interval between check
  static final int DEFAULT_DELAY = 3000;

  // Background thread
  static Thread backgroundThread;

  @FXML
  private Button startButton;

  @FXML
  private Button stopButton;

  @FXML
  private Label statusBar;

  @FXML
  private ComboBox delaySelector;

  private Task worker;

  private Point prev_point;

  private Integer delay = DEFAULT_DELAY;

  @FXML
  public void start(ActionEvent actionEvent)
  {
    statusBar.setText("Track mouse position.");
    stopButton.setDisable(false);
    startButton.setDisable(true);

    prev_point = MouseInfo.getPointerInfo().getLocation();
    worker = createWorker();

    backgroundThread = new Thread(worker);
    backgroundThread.start();
  }

  @FXML
  public void initialize() {
    binding();

    // Set initial delay
    delaySelector.setValue(delay/1000);
  }

  private void binding()
  {
    delaySelector.getSelectionModel()
        .selectedItemProperty()
        .addListener((ObservableValue observable, Object oldDelay, Object newDelay) ->
      {
        if(!(newDelay instanceof Integer))
          return;

        info("Update delaySelector: " + newDelay);
        delay = (Integer)newDelay * 1000;
      });
  }

  public Task createWorker() {
    return new Task() {
      @Override
      protected Object call() throws Exception {
        boolean state = true;
        while(true)
        {
          info("Wait: " +delay);
          Thread.sleep(delay);

          Point point = MouseInfo.getPointerInfo().getLocation();
          if(point.x != prev_point.x || point.y != prev_point.y)
          {
            info("Motion detected");
            prev_point = point;
            continue;
          }

          Platform.runLater(() -> {
              DateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm:ss");
              statusBar.setText("Move at " + dateFormat.format(Calendar.getInstance().getTime()));
          });

          info("Move is asleep");

          // Update mouse position
          prev_point = moveMouseRandomly(point, state);

          // Change state
          state = !state;
        }
      }
    };
  }

  public Point moveMouseRandomly(Point point, boolean state)
  {
//    Random random = new Random();
//    int dx = random.nextInt(20);
//    int dy = random.nextInt(20);
    int dx, dy;
    if(state)
    {
      dx = 10;
      dy = 0;
    }
    else
    {
      dx = -10;
      dy = 0;
    }
    Point new_point = new Point(point.x + dx, point.y + dy);
    moveCursor(new_point.x, new_point.y);
    return new_point;
  }

  /**
   * Move the mouse to the specific screen position
   */
  public void moveCursor(final int screenX, final int screenY) {
    Platform.runLater(() ->
    {
        try {
          Robot robot = new Robot();
          robot.mouseMove(screenX, screenY);
        } catch (AWTException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
    });
  }

  @FXML
  public void stop(ActionEvent actionEvent)
  {
    actionEvent.consume();

    statusBar.setText("Stop mouse random motion...");

    stopButton.setDisable(true);
    startButton.setDisable(false);

    worker.cancel();
    info("Stop background thread");
  }

  public static void info(String s)
  {
    System.out.println(s);
  }

  public void exit(ActionEvent actionEvent)
  {
    actionEvent.consume();

    if(worker != null)
    {
      worker.cancel();
    }

    System.exit(0);
  }
}
