package köntröller;

import processing.core.PApplet;

public class LoopOver extends PApplet {

    private int size;
    private int scale = 50;
    String gameStatus = "";
    private int[][] gameField;
    private Network ai;

    public void setup() {
        surface.setResizable(true);
        noStroke();
        textAlign(CENTER,CENTER);
        newGame(5);
    }

    public void settings() {
        size(500,500);
    }

    public void draw() {
        background(0);

        //Sets scale
        if (width / (gameField.length + 5) < height / (gameField.length + 2)) {
            scale = width / (gameField.length + 5);
        } else {
            scale = height / (gameField.length + 2);
        }
        textSize((scale*5)/10);

        //Draws the field
        for (int i = 0; i < gameField.length; i++) {
            for (int j = 0; j < gameField[0].length; j++) {
                int jc = gameField[j][i] % size;
                int ic = gameField[j][i] / size;
                if (jc == 0) jc = size;
                fill(255-(255*jc-255)/(size),(255*ic)/(size),(255*jc)/(size));
                rect(j * scale + scale, i * scale + scale,scale,scale);
                fill(0);
                text(gameField[j][i],j * scale + scale + scale/2, i * scale + scale + scale/2);
            }
        }

        //Draw the Buttons to move
        fill(255);
        //top buttons
        for (int i = 1; i <= size; i++) {
            text("▲", i * scale + scale/2,scale/2);
        }
        //left buttons
        for (int i = 1; i <= size; i++) {
            text("◀",scale/2, i * scale + scale/2);
        }
        //bottom buttons
        for (int i = 1; i <= size; i++) {
            text("▼", i * scale + scale/2,scale/2 + (size+1) * scale);
        }
        //right buttons
        for (int i = 1; i <= size; i++) {
            text("▶",scale/2 + (size+1) * scale, i * scale + scale/2);
        }

        //Scramble button
        fill(25);
        rect(scale * (size+2), 0, scale * 3, scale);
        fill(255);
        text("Scramble",scale * (size+3) + scale/2, scale/2);

        //Start AI solve
        fill(25);
        rect(scale * (size+2), scale, scale * 3, scale);
        fill(255);
        text("Start AI",scale * (size+3) + scale/2, scale/2 + scale);

        //Stop AI solve
        fill(25);
        rect(scale * (size+2), scale * 2, scale * 3, scale);
        fill(255);
        text("Stop AI",scale * (size+3) + scale/2, scale/2 + scale * 2);

        //Bigger field
        fill(25);
        rect(scale * (size+2), scale * 3, scale * 3, scale);
        fill(255);
        text("Size ▲",scale * (size+3) + scale/2, scale/2 + scale * 3);

        //Smaller field
        fill(25);
        rect(scale * (size+2), scale * 4, scale * 3, scale);
        fill(255);
        text("Size ▼",scale * (size+3) + scale/2, scale/2 + scale * 4);

        //Gamestatus
        fill(25);
        rect(scale * (size+2), scale * 5, scale * 3, scale);
        fill(255,255,0);
        if (gameStatus.equals("Solved")) {
            fill(0,255,0);
        }
        text(gameStatus,scale * (size+3) + scale/2, scale/2 + scale * 5);

    }

    public void mousePressed() {
        //Up buttons
        if (mouseX > scale && mouseX < scale*(size+1) && mouseY > 0 && mouseY < scale ) {
            int t = (mouseX-(mouseX%scale))/scale;
            trainAI(t);
            turn(t);
        }

        //Right buttons
        if (mouseX > scale * (size + 1) && mouseX < scale * (size + 2) && mouseY > scale && mouseY < scale * (size + 2) ) {
            int t = (mouseY-(mouseY%scale))/scale + size;
            trainAI(t);
            turn(t);
        }

        //Down buttons
        if (mouseX > scale && mouseX < scale*(size+1) && mouseY > scale * (size + 1) && mouseY < scale * (size + 2) ) {
            int t = (mouseX-(mouseX%scale))/scale + size * 2;
            trainAI(t);
            turn(t);
        }

        //Left buttons
        if (mouseX > 0 && mouseX < scale && mouseY > scale && mouseY < scale * (size + 2) ) {
            int t = (mouseY-(mouseY%scale))/scale + size * 3;
            trainAI(t);
            turn(t);
        }

        //Menu buttons
        if (mouseX > scale * (size + 2) && mouseX < scale * (size + 5) && mouseY > 0 && mouseY < scale * 5) {
            int t = (mouseY-(mouseY%scale))/scale;
            buttonPressed(t);
        }
    }

    private void turn(int direction) {
        direction--;
        int tmp;
        int index = direction%size;
        switch (direction/size) {
            case 0:
                System.out.print("up ");
                tmp = gameField[index][0];
                for (int i = 0; i < size-1; i++) {
                    gameField[index][i] = gameField[index][i+1];
                }
                gameField[index][size-1] = tmp;
                break;
            case 1:
                System.out.print("right ");
                tmp = gameField[size-1][index];
                for (int i = size-1; i > 0; i--) {
                    gameField[i][index] = gameField[i-1][index];
                }
                gameField[0][index] = tmp;
                break;
            case 2:
                System.out.print("down ");
                tmp = gameField[index][size-1];
                for (int i = size-1; i > 0; i--) {
                    gameField[index][i] = gameField[index][i-1];
                }
                gameField[index][0] = tmp;
                break;
            case 3:
                System.out.print("left ");
                tmp = gameField[0][index];
                for (int i = 0; i < size-1; i++) {
                    gameField[i][index] = gameField[i+1][index];
                }
                gameField[size-1][index] = tmp;
                break;
        }
        System.out.println(direction%size);
        if (won()) {
            gameStatus = "Solved";
        } else {
            gameStatus = "Solving";
        }
        double[] idk = ai.calculate(getDataForAI());
        int max = 0;
        for (int i = 1; i < size*4; i++) {
            if (idk[max] < idk[i]) {
                max = i;
            }
        }
        System.out.println(max);
    }

    private void trainAI(int direction) {
        double[] trainData = getDataForAI();
        double[] expecteDresult = new double[size*4];

        TrainSet trainset = new TrainSet(1,1);
        trainset.addData(trainData, expecteDresult);
        ai.train(trainset, 1000000, 0);
    }

    private boolean won() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (gameField[j][i] != i*gameField.length + j + 1) {
                    return false;
                }
            }
        }
        return true;
    }

    private void newGame(int fieldSize) {
        size = fieldSize;
        //Fill ze gameField j = x, i = y    xy
        gameField = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                gameField[j][i] = i*gameField.length + j + 1;
            }
        }
        ai = new Network(size * size * size * size, size * size * size, size * size, size * 4);
    }

    private void buttonPressed(int buttonID) {
        switch (buttonID) {
            case 0:
                for (int i = 0; i < size*size*size; i++) {
                    turn((int)(Math.random()*size*4)+1);
                }
                break;
            case 3:
                newGame(size+1);
                break;
            case 4:
                newGame(size-1);
                break;
        }
    }

    private double[] getDataForAI() {
        double[] data = new double[size*size*size*size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                data[i*size*j*size + gameField[j][i]] = 1;
            }
        }
        return data;
    }
}
