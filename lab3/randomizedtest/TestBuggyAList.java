package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE
  @Test
  public void testThreeAddThreeRemove() {
      AListNoResizing<Integer> correct = new AListNoResizing<>();
      BuggyAList<Integer> buggy = new BuggyAList<>();
      for (int i= 0; i < 3; i++) {
          correct.addLast(i);
          buggy.addLast(i);
      }
      for (int j= 0; j < 3; j++) {
          int expected = correct.removeLast();
          int actual = buggy.removeLast();
          assertEquals(expected, actual);
      }
  }
  @Test
  public void randomizedTest() {
      AListNoResizing<Integer> correct = new AListNoResizing<>();
      BuggyAList<Integer> buggy = new BuggyAList<>();
      int N = 1000000;

      for (int i= 0; i < N; i++) {
          int operation = StdRandom.uniform(0, 4);
          if (operation == 0) {
              int expected = StdRandom.uniform(0, 100);
              correct.addLast(expected);
              buggy.addLast(expected);
              // System.out.println("addLast(" + expected + ")");
          } else if (operation == 1) {
              int expected = correct.size();
              int actual = buggy.size();
              assertEquals(expected, actual);
              // System.out.println("size: " + expected);
          } else if (operation == 2) {
              if (correct.size() > 0) {
                  int expected = correct.getLast();
                  int actual = buggy.getLast();
                  assertEquals(expected, actual);
                  // System.out.println("getLast: " + expected);
              }
          } else {
              if (correct.size() > 0) {
                  int expected = correct.removeLast();
                  int actual = buggy.removeLast();
                  assertEquals(expected, actual);
                  // System.out.println("removeLast: " + expected);
              }
          }
      }
  }
}
