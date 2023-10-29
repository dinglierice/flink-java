package com.zy.self.experience.concurrent.cpt3;


public class NoVisibility {
    private static boolean ready;
    private static int number;

    private static class RenderThread extends Thread {
        @Override
        public void run() {
            while (!ready) {
                Thread.yield();
            }
            System.out.println(number);
        }
    }

    public static void main(String[] args) {
        new RenderThread().start();
        number = 42;
        ready = true;
    }
}
