import java.awt.*;

class Stone {
    public final static int black = 1;
    public final static int white = 2;
    private int obverse;
    // コンストラクタの初期化
    Stone(){
        obverse = 0; //初期値は0とし、非配置とする
    }
    
    // 表面の色を設定
    void setObverse(int color) {
        if(color == black || color == white) {
            obverse = color;
        }
        else {
            System.out.println("黒か白でなければいけません");
        }
    }

    //表面の色で中心p、半径radの円を塗りつぶす
    void paint(Graphics g, Point p, int rad) {
        if (obverse == black) {
            g.setColor(Color.black); //ペンを黒に設定
            g.fillOval(p.x-rad, p.y-rad, rad*2, rad*2); //円を描画
        }
        else if (obverse == white) {
            g.setColor(Color.white); //ペンを白に設定
            g.fillOval(p.x-rad, p.y-rad, rad*2, rad*2); //円を描画
        }
    }

    //表面の色を取得
    int getObverse(){
        return obverse;
    }

    //白黒を反転
    void doReverse(){
        if(obverse == black) obverse = white;
        else if(obverse == white) obverse = black;
    }
}