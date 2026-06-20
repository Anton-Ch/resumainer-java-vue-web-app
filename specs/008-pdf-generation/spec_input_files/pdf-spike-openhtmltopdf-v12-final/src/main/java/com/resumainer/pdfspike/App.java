package com.resumainer.pdfspike;

import com.resumainer.pdfspike.runner.SpikeRunner;

public final class App {
    private App() {}

    public static void main(String[] args) throws Exception {
        SpikeRunner.fromArgs(args).run();
    }
}
