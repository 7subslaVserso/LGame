using Loon.Core.Graphics;
using Loon.Action.Map;
using Loon.Core.Graphics.Component;
using Loon.Utils;
using Loon.Action;
using Loon.Core.Graphics.Opengl;
using System.Collections;
using Loon.Action.Sprite;
using Loon.Core.Graphics.Device;
using System;
using Loon.Core;
using Loon.Core.Resource;
using Loon.Core.Timer;
using Loon.Core.Input;
namespace TDSample
{
    public class TDScreen : Screen
    {

        private static int selectTurret = -1;

        private static Field2D Field;

        private static string[] turrets = new string[] { "assets/bulletTurret.png",
			"assets/bombTurret.png", "assets/poisonTurret.png",
			"assets/laserTurret.png", "assets/bullet.png" };

        /**
         * �ӵ�����
         * 
         */
        class Bullet : Actor
        {

            /* �������ü��ٶȡ����� */
            private float vx, vy, gravity;

            /* ����һ��type�����������ӵ����� */
            int type;

            private float speed;

            private float dir;

            private int damage;

            private float x, y;

            private bool removeFlag;

            public Bullet(int type, string fileName, float dir, int damage)
            {
                this.type = type;
                this.dir = dir;
                this.damage = damage;
                this.SetImage(fileName);
                this.SetDelay(50);
                /* ����ӵ�����Ϊ������ */
                if (type == 1)
                {
                    this.SetDelay(0);
                    this.speed = 200f;
                    this.gravity = 200f;
                    // ת������(360�ȷ���)Ϊ����
                    float angle = MathUtils.ToRadians(this.dir);
                    // ������ٶ�
                    this.vx = speed * MathUtils.Cos(angle);
                    this.vy = speed * MathUtils.Sin(angle);
                }
            }

            protected override void AddLayer(ActorLayer layer)
            {
                this.x = this.GetX();
                this.y = this.GetY();
            }

            public override void Action(long t)
            {
                if (removeFlag)
                {
                    return;
                }
                object o = null;

                switch (type)
                {
                    case 0:
                        for (int i = 0; i < 6; i++)
                        {
                            // ��������λ��
                            float angle = MathUtils.ToRadians(this.dir);
                            this.x += MathUtils.Cos(angle);
                            this.y += MathUtils.Sin(angle);
                        }
                        this.SetLocation(
                                this.x + (Field.GetTileWidth() - this.GetWidth()) / 2,
                                this.y + (Field.GetTileHeight() - this.GetHeight()) / 2);
                        break;
                    case 1:
                        /*
                         * ����ÿ���ӵ�λ��(����������ֱ���������ߣ���һ�����ף���MapLayer��setDelay���趨����500
                         * Ҳ����ÿ��500���루���룩���Ÿı�һ�ε�ͼ������״̬�������������������߻᲻̫��Ȼ��)
                         */
                        x = GetX();
                        y = GetY();
                        float dt = MathUtils.Max((t / 1000), 0.01f);
                        vy += gravity * dt;
                        x += vx * dt;
                        y += vy * dt;
                        this.SetLocation(this.x, this.y);
                        break;
                }

                o = this.GetOnlyCollisionObject(typeof(Enemy));
                // �������ײʱ
                if (o != null)
                {
                    Enemy e = (Enemy)o;
                    // ���ٵз�HP
                    e.hp -= this.damage;
                    e.hpBar.SetUpdate(e.hp);
                    removeFlag = true;
                    // ��Layer��ɾ������
                    GetLLayer().RemoveObject(this);

                    return;
                    // ������Ϸ����ʱɾ������
                }
                else if (this.GetX() <= 12
                      || this.GetX() >= this.GetLLayer().GetWidth() - 12
                      || this.GetY() <= 12
                      || this.GetY() >= this.GetLLayer().GetHeight() - 12)
                {
                    removeFlag = true;
                    this.GetLLayer().RemoveObject(this);
                }
            }
        }

        /**
         * ��������
         * 
         */
        class Turret : Actor
        {

            private int range = 50;

            private int delay = 10;

            internal bool selected;

            public Turret(string fileName)
            {
                SetImage(fileName);
                SetDelay(100);
                SetAlpha(0);
            }

            class RotationAction : ActionListener
            {
                public void Start(ActionBind o)
                {

                }

                public void Process(ActionBind o)
                {

                }

                public void Stop(ActionBind o)
                {
                    ((Actor)o).RotateTo(90);
                }
            }

            protected override void AddLayer(ActorLayer layer)
            {
                // �ý�ɫ����ʽ����
                FadeTo fade = FadeOut();
                fade.SetActionListener(new RotationAction());
            }

            public override void Draw(GLEx g)
            {
                if (selected)
                {
                    g.SetColor(255, 0, 0, 100);
                    g.FillOval(-(range * 2 - Field.GetTileWidth()) / 2,
                            -(range * 2 - Field.GetTileHeight()) / 2,
                            this.range * 2 - 1, this.range * 2 - 1);
                    g.SetColor(LColor.red);
                    g.DrawOval(-(range * 2 - Field.GetTileWidth()) / 2,
                            -(range * 2 - Field.GetTileHeight()) / 2,
                            this.range * 2 - 1, this.range * 2 - 1);
                    g.ResetColor();
                }
            }

            public override void Action(long t)
            {
                // ����ָ���뾶������Enemy��
                IList es = this.GetCollisionObjects(this.range, typeof(Enemy));
                // �����˴���
                if (es.Count != 0)
                {
                    Enemy target = (Enemy)es[0];
                    // ��ת��̨��׼Enemy����
                    SetRotation((int)MathUtils.ToDegrees(MathUtils.Atan2(
                            (target.GetY() - this.GetY()),
                            (target.GetX() - this.GetX()))));

                }
                // �ӳ��ڻ�
                if (this.delay > 0)
                {
                    --this.delay;
                }
                else if (es.Count != 0)
                {

                    // *���ӵ������趨Ϊ1*/
                    // �����ڵ�
                    Bullet bullet = new Bullet(0, turrets[4], this.GetRotation(), 2);

                    // �����ڻ���
                    int x = MathUtils.Round(MathUtils.Cos(MathUtils.ToRadians(this
                            .GetRotation())) * (float)bullet.GetWidth() * 2)
                            + this.X();

                    int y = MathUtils.Round(MathUtils.Sin(MathUtils.ToRadians(this
                            .GetRotation())) * (float)bullet.GetHeight() * 2)
                            + this.Y();

                    // ע���ڵ���Layer
                    this.GetLLayer().AddObject(bullet, x, y);
                    this.delay = 10;

                }

            }
        }

        /**
         * �б�����
         * 
         */
        class Enemy : Actor
        {

            private int startX, startY;

            private int endX, endY;

            internal int speed, hp;

            private bool removeFlag;

            // ʹ�þ���StatusBar�䵱Ѫ��
            internal StatusBar hpBar;

            public Enemy(string fileName, int sx, int sy, int ex, int ey,
                    int speed, int hp)
            {
                this.SetDelay(300);
                this.SetImage(fileName);
                this.hpBar = new StatusBar(hp, hp, (this.GetWidth() - 25) / 2,
                        this.GetHeight() + 5, 25, 5);
                this.startX = sx;
                this.startY = sy;
                this.endX = ex;
                this.endY = ey;
                this.speed = speed;
                this.hp = hp;
            }

            public override void Draw(GLEx g)
            {

                // ���ƾ���
                hpBar.CreateUI(g);

            }

            public class RemoveAction : ActionListener
            {

                private Enemy enemy;

                public RemoveAction(Enemy e)
                {
                    this.enemy = e;
                }

                public void Start(ActionBind o)
                {

                }

                public void Process(ActionBind o)
                {

                }

                public void Stop(ActionBind o)
                {
                    enemy.RemoveActionEvents();
                    enemy.GetLLayer().RemoveObject(enemy);
                }
            }


            public override void Action(long t)
            {
                // ���������¼�
                hpBar.Update(t);
                if (hp <= 0 && !removeFlag)
                {
                    // �趨����ʱ����
                    FadeTo fade = FadeIn();
                    // ����ʱ��Ϊ30����
                    fade.SetSpeed(30);
                    // �����������
                    fade.SetActionListener(new RemoveAction(this));

                    this.removeFlag = true;
                }
            }

            class MoveAction : ActionListener
            {

                ActorLayer layer;

                MoveTo move;

                public MoveAction(MoveTo move, ActorLayer layer)
                {
                    this.move = move;
                    this.layer = layer;
                }

                // ��ȡ�¼�����������
                public void Process(ActionBind o)
                {
                    // ��ý�ɫ�ƶ�����
                    switch (move.GetDirection())
                    {
                        case Field2D.TUP:
                            // ���ݵ�ǰ�ƶ����򣬱����ɫ��ת��������ͬ��
                            o.SetRotation(270);
                            break;
                        case Field2D.TLEFT:
                            o.SetRotation(180);
                            break;
                        case Field2D.TRIGHT:
                            o.SetRotation(0);
                            break;
                        case Field2D.TDOWN:
                            o.SetRotation(90);
                            break;
                        default:
                            break;
                    }

                }

                public void Start(ActionBind o)
                {

                }

                // ����ɫ�ƶ����ʱ
                public void Stop(ActionBind o)
                {
                    // ��Layer��ɾ���˽�ɫ
                    layer.RemoveObject((Actor)o);
                }

            }


            // �״�ע��Layerʱ���ô˺���
            protected override void AddLayer(ActorLayer layer)
            {

                // ��������������ý�ɫ������Ƭ����
                int offsetX = (GetLLayer().GetField2D().GetTileWidth() - this
                       .GetWidth()) / 2;
                int offsetY = (GetLLayer().GetField2D().GetTileWidth() - this
                       .GetHeight()) / 2;
                // ��ʼ����ɫ��Layer������
                SetLocation(startX + offsetX, startY + offsetY);
                // �����ɫ��ָ�����������ƶ�(����ΪfalseΪ�ķ���Ѱ����Ϊtrueʱ�˷���)���������ƶ�������
                // PS:endX��endY����ʾλ�ã����Բ��ؽ���
                MoveTo move = MoveTo(endX, endY, false);

                // �������꣬�ý�ɫ����
                move.SetOffset(offsetX, offsetY);
                // ������ɫ�¼�����
                move.SetActionListener(new MoveAction(move, layer));
                // �趨�ƶ��ٶ�
                move.SetSpeed(speed);
            }
        }

        // ��ʼ��
        class Begin : Actor
        {
            public Begin(string fileName)
            {
                SetImage(fileName);
            }
        }

        // ������
        class End : Actor
        {
            public End(string fileName)
            {
                SetImage(fileName);
            }
        }

        /**
         * ��ק�ò˵�
         * 
         */
        class Menu : LLayer
        {

            class BulletTurret : LPaper
            {

                public BulletTurret()
                    : base(turrets[0])
                {
                }

                // ��ѡ�е�ǰ��ťʱ��Ϊ��ť����ѡ�п�(����ͬ)
                public override void Paint(GLEx g)
                {
                    if (selectTurret == 0)
                    {
                         g.SetColor(LColor.red);
                         g.DrawRect(2, 2, this.GetWidth() - 4,
                                 this.GetHeight() - 4);
                         g.ResetColor();
                    }
                }

                public override void DownClick()
                {
                    selectTurret = 0;
                }
            };

            class BombTurret : LPaper
            {

                public BombTurret()
                    : base((turrets[1]))
                {

                }

                public override void Paint(GLEx g)
                {
                    if (selectTurret == 1)
                    {
                        g.SetColor(LColor.red);
                        g.DrawRect(2, 2, this.GetWidth() - 4,
                                this.GetHeight() - 4);
                        g.ResetColor();
                    }
                }

                public override void DownClick()
                {
                    selectTurret = 1;
                }
            };

            class PoisonTurret : LPaper
            {

                public PoisonTurret()
                    : base(turrets[2])
                {
                }

                public override void Paint(GLEx g)
                {
                    if (selectTurret == 2)
                    {
                        g.SetColor(LColor.red);
                        g.DrawRect(2, 2, this.GetWidth() - 4,
                                this.GetHeight() - 4);
                        g.ResetColor();
                    }
                }

                public override void DownClick()
                {
                    selectTurret = 2;
                }
            };

            class LaserTurret : LPaper
            {

                public LaserTurret()
                    : base(turrets[3])
                {
                }

                public override void Paint(GLEx g)
                {
                    if (selectTurret == 3)
                    {
                        g.SetColor(LColor.red);
                        g.DrawRect(2, 2, this.GetWidth() - 4,
                                this.GetHeight() - 4);
                        g.ResetColor();
                    }
                }

                public override void DownClick()
                {
                    selectTurret = 3;
                }
            };

            // ��LPaper�����������Ӱ�ť
            class Button : LPaper
            {


                public Button()
                    : base("assets/button.png")
                {

                }

                public override void DownClick()
                {
                    // ���MapLayer
                    MapLayer layer = (MapLayer)Screen.StaticCurrentSceen.GetBottomLayer();
                    // ��ʼ��Ϸ����
                    layer.DoStart();
                }
            };

            public Menu()
                : base(128, 240)
            {


                // �趨menu�㼶����MapLayer
                SetLayer(101);
                // ������menu�ƶ�
                SetLocked(false);
                SetLimitMove(false);
                // ����Actor��ק
                SetActorDrag(false);
                SetDelay(500);
                // �趨Menu����
                LImage image = LImage.CreateImage(this.GetWidth(),
                        this.GetHeight(), true);
                LGraphics g = image.GetLGraphics();
                g.SetColor(0, 0, 0, 125);
                g.FillRect(0, 0, GetWidth(), GetHeight());
                g.SetColor(LColor.white);
                g.SetFont(15);
                g.DrawString("���ǿ���ק�˵�", 12, 25);
                g.Dispose();
                SetBackground(image.GetTexture());

                BulletTurret bulletTurret = new BulletTurret();
                bulletTurret.SetLocation(18, 64);


                BombTurret bombTurret = new BombTurret();
                bombTurret.SetLocation(78, 64);


                PoisonTurret poisonTurret = new PoisonTurret();
                poisonTurret.SetLocation(18, 134);


                LaserTurret laserTurret = new LaserTurret();
                laserTurret.SetLocation(78, 134);

                Button button = new Button();
                button.SetLocation(27, 196);

                // ����LPaper��Layer
                Add(bulletTurret);
                Add(bombTurret);
                Add(poisonTurret);
                Add(laserTurret);
                Add(button);
            }

            public override void DownClick(int x, int y)
            {
                selectTurret = -1;
            }

        }

        /**
         * ���ͼ��Layer
         */
        class MapLayer : LLayer
        {

            private bool start;

            private int startX, startY, endX, endY;

            private int index, count;
            // ����MapLayer����Ԫ��(��ֵ��Ҫ��map.txt�ļ��б�ʶ���Ӧ)
            private System.Collections.Generic.Dictionary<object, object> pathMap = new System.Collections.Generic.Dictionary<object, object>();

            public MapLayer()
                : base(576, 480, true)
            {

                // ������MapLayer��ק
                SetLocked(false);
                // ����MapLayer�н�ɫ��ק
                SetActorDrag(false);

                pathMap.Add(0, new LImage("assets/sand.png"));
                pathMap.Add(1, new LImage("assets/sandTurn1.png"));
                pathMap.Add(2, new LImage("assets/sandTurn2.png"));
                pathMap.Add(3, new LImage("assets/sandTurn3.png"));
                pathMap.Add(4, new LImage("assets/sandTurn4.png"));
                pathMap.Add(5, new Begin("assets/base.png"));
                pathMap.Add(6, new End("assets/castle.png"));

                ConfigReader config = ConfigReader.GetInstance("assets/map.txt");

                // ΪLayer����򵥵�2D��ͼ��������Ƭ��С32x32����rockͼƬ�̵�
                SetField2DBackground(config.GetField2D("test", 32, 32), pathMap,
                        "assets/rock.png");


                Field = GetField2D();

                // ���˳�������
                this.startX = 64;
                this.startY = 416;
                // ������ʧ����
                this.endX = 480;
                this.endY = 416;

                // �趨MapLayerÿ��2��ִ��һ���ڲ�Action
                SetDelay(LSystem.SECOND * 2);
            }

            public override void Action(long t)
            {
                // ��������ʶΪtrueʱִ�����²���
                if (start)
                {
                    if (index < 3)
                    {
                        Enemy enemy = null;
                        // ���ݵ��next(���ӵ���)�Ĵ����任������ʽ
                        switch (count)
                        {
                            case 0:
                                enemy = new Enemy("assets/enemy.png", startX, startY,
                                        endX, endY, 2, 4);
                                break;
                            case 1:
                                enemy = new Enemy("assets/fastEnemy.png", startX,
                                        startY, endX, endY, 4, 6);
                                break;
                            case 2:
                                enemy = new Enemy("assets/smallEnemy.png", startX,
                                        startY, endX, endY, 3, 10);
                                break;
                            case 3:
                                enemy = new Enemy("assets/bigEnemy.png", startX,
                                        startY, endX, endY, 1, 16);
                                break;
                            default:
                                count = 0;
                                enemy = new Enemy("assets/enemy.png", startX, startY,
                                        endX, endY, 2, 2);
                                break;
                        }
                        AddObject(enemy);
                        index++;
                        // ����λ
                    }
                    else
                    {
                        start = false;
                        index = 0;
                        count++;
                    }
                }
            }

            private Actor o = null;

            public override void DownClick(int x, int y)
            {
                // ת�����������Ϊ�����ͼ����
                int newX = x / Field.GetTileWidth();
                int newY = y / Field.GetTileHeight();
                // ��ѡ������(������Ϊ-1)�������ͼ����Ϊ-1(����ͨ��)������������ɫ�ڴ�ʱ
                if ((o = GetClickActor()) == null && selectTurret != -1
                        && Field.GetType(newY, newX) == -1)
                {
                    // �������
                    AddObject(new Turret(turrets[selectTurret]),
                            newX * Field.GetTileWidth(),
                            newY * Field.GetTileHeight());
                }
                if (o != null && o is Turret)
                {
                    ((Turret)o).selected = true;
                }
            }

            public override void UpClick(int x, int y)
            {
                if (o != null && o is Turret)
                {
                    ((Turret)o).selected = false;
                }
            }

            public void DoStart()
            {
                this.start = true;
            }

        }

        public override void OnLoad()
        {

            // ������ͼ��Layer
            MapLayer layer = new MapLayer();
            layer.SetAutoDestroy(true);
            // ����
            CenterOn(layer);
            // ���MapLayer��Screen
            Add(layer);
            // �����˵���Layer
            Menu menu = new Menu();
            // ��menu������Ļ�Ҳ�
            RightOn(menu);
            menu.SetY(0);
            // ���menu��Screen
            Add(menu);
        }

        public override void Alter(LTimerContext timer)
        {

        }

        public override void Draw(GLEx g)
        {

        }

        public override void TouchDown(LTouch touch)
        {

        }

        public override void TouchUp(LTouch touch)
        {

        }

        public override void TouchMove(LTouch e)
        {

        }

        public override void TouchDrag(LTouch arg0)
        {

        }
    }
}
