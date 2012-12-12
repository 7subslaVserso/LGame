
using Loon.Action.Sprite;
using Loon.Action.Map;
using Loon.Core.Geom;
using Loon.Core.Graphics;
using Loon.Core.Event;
using Loon.Core.Input;
using Loon.Core.Graphics.Component;
using Loon.Core;
using Loon.Action;
namespace ACTSample
{
    public class GameMapTest : SpriteBatchScreen
    {
        // ��������
        class Enemy : SpriteBatchObject
        {

            private float SPEED = 1;

            protected float vx;
            protected float vy;

            public Enemy(float x, float y, Animation animation, TileMap tiles)
                : base(x, y, 32, 32, animation, tiles)
            {
                vx = -SPEED;
                vy = 0;
            }

            public override void Update(long elapsedTime)
            {

                float x = GetX();
                float y = GetY();

                vy += 0.6f;

                float newX = x + vx;

                // �ж�Ԥ�������Ƿ�����Ƭ��ײ(X�������)
                Vector2f tile = tiles.GetTileCollision(this, newX, y);

                if (tile == null)
                {
                    x = newX;
                }
                else
                {
                    if (vx > 0)
                    {
                        x = tiles.TilesToPixelsX(tile.x) - GetWidth();
                    }
                    else if (vx < 0)
                    {
                        x = tiles.TilesToPixelsY(tile.x + 1);
                    }
                    vx = -vx;
                }

                float newY = y + vy;

                // �ж�Ԥ�������Ƿ�����Ƭ��ײ(y�������)
                tile = tiles.GetTileCollision(this, x, newY);
                if (tile == null)
                {
                    y = newY;
                }
                else
                {
                    if (vy > 0)
                    {
                        y = tiles.TilesToPixelsY(tile.y) - GetHeight();
                        vy = 0;
                    }
                    else if (vy < 0)
                    {
                        y = tiles.TilesToPixelsY(tile.y + 1);
                        vy = 0;
                    }
                }

                animation.Update(elapsedTime);
                // ע��������
                SetLocation(x, y);
            }

        }

        // ������Ծ���ࣨ��Ʒ��
        class JumperTwo : SpriteBatchObject
        {

            public JumperTwo(float x, float y, Animation animation, TileMap tiles)
                : base(x, y, 32, 32, animation, tiles)
            {

            }

            public void Use(JumpObject hero)
            {
                hero.SetJumperTwo(true);
            }

            public override void Update(long elapsedTime)
            {
                animation.Update(elapsedTime);
            }
        }


        // �������ࣨ��Ʒ��
        class Accelerator : SpriteBatchObject
        {

            public Accelerator(float x, float y, Animation animation, TileMap tiles)
                : base(x, y, 32, 32, animation, tiles)
            {

            }

            public void Use(JumpObject hero)
            {
                hero.SetSpeed(hero.GetSpeed() * 2);
            }

            public override void Update(long elapsedTime)
            {
                animation.Update(elapsedTime);
            }
        }

        // ������ࣨ��Ʒ��
        class Coin : SpriteBatchObject
        {

            public Coin(float x, float y, Animation animation, TileMap tiles)
                : base(x, y, 32, 32, animation, tiles)
            {

            }

            public override void Update(long elapsedTime)
            {
                animation.Update(elapsedTime);
            }

        }

        private JumpObject hero;

        // PS�����������Ϸ����ʱ�õ��ද���л�������ʹ��AnimationStorage���Animation������
        // ����ö���ͼ
        private Animation coinAnimation;

        // �����ö���ͼ(���˵���ɫ)
        private Animation enemyAnimation;

        // ���ٵ��߶���ͼ
        private Animation accelAnimation;

        // ����������ͼ
        private Animation jumpertwoAnimation;

        public override void Create()
        {

            // ��ָ��ͼƬ��������
            this.coinAnimation = Animation.GetDefaultAnimation("assets/coin.png",
                    32, 32, 200);
            this.enemyAnimation = Animation.GetDefaultAnimation("assets/enemy.gif",
                    32, 32, 200, LColor.black);
            this.accelAnimation = Animation.GetDefaultAnimation(
                    "assets/accelerator.gif", 32, 32, 200);
            this.jumpertwoAnimation = Animation.GetDefaultAnimation(
                    "assets/jumper_two.gif", 32, 32, 200);

            // ע��Screenʱ�ͷ�������Դ
            PutReleases(coinAnimation, enemyAnimation, accelAnimation,
                    jumpertwoAnimation, hero);

            // ����һ�����ַ����γɵĵ�ͼ�������ʹ�ô˷�ʽ���أ���Ĭ��ʹ�ñ�׼�������ͼ��
            TileMap indexMap = TileMap.LoadCharsMap("assets/map.chr", 32, 32);
            // ��������úõ�LTexturePack�ļ������ڴ�ע��
            // indexMap.setImagePack(file);
            // �趨�޷���Խ������(��������ô����������������"-1"�����򶼿��Դ�Խ)
            indexMap.SetLimit(new int[] { 'B', 'C', 'i', 'c' });
            indexMap.PutTile('B', "assets/block.png");
            int imgId = indexMap.PutTile('C', "assets/coin_block.gif");
            // ��Ϊ������Ƭ��Ӧͬһ��ͼ�ַ������Դ˴�ʹ�����������ͼƬ����
            indexMap.PutTile('i', imgId);
            indexMap.PutTile('c', "assets/coin_block2.gif");

            // ���ش˵�ͼ��������
            putTileMap(indexMap);

            // ��õ�ͼ��Ӧ�Ķ�ά����
            int[][] maps = indexMap.GetMap();

            int w = indexMap.GetRow();
            int h = indexMap.GetCol();

            // ������ά�����ͼ�����Դ�Ϊ������ӽ�ɫ������֮��
            for (int i = 0; i < w; i++)
            {
                for (int j = 0; j < h; j++)
                {
                    switch (maps[j][i])
                    {
                        case 'o':
                            Coin coin = new Coin(indexMap.TilesToPixelsX(i),
                                    indexMap.TilesToPixelsY(j), new Animation(
                                            coinAnimation), indexMap);
                            AddTileObject(coin);
                            break;
                        case 'k':
                            Enemy enemy = new Enemy(indexMap.TilesToPixelsX(i),
                                    indexMap.TilesToPixelsY(j), new Animation(
                                            enemyAnimation), indexMap);
                            AddTileObject(enemy);
                            break;
                        case 'a':
                            Accelerator accelerator = new Accelerator(
                                    indexMap.TilesToPixelsX(i),
                                    indexMap.TilesToPixelsY(j), new Animation(
                                            accelAnimation), indexMap);
                            AddTileObject(accelerator);
                            break;
                        case 'j':
                            JumperTwo jump = new JumperTwo(indexMap.TilesToPixelsX(i),
                                    indexMap.TilesToPixelsY(j), new Animation(
                                            jumpertwoAnimation), indexMap);
                            AddTileObject(jump);
                            break;
                    }
                }
            }

            // ������Ƕ���ͼ
            Animation animation = Animation.GetDefaultAnimation("assets/hero.png",
                    20, 20, 150, LColor.black);

            // ����������λ��(192,32)���ý�ɫ����СΪ32x32������Ϊ���hero.png�ķֽ�ͼ
            hero = AddJumpObject(192, 32, 32, 32, animation);

            // �õ�ͼ����ָ����������ƶ������۲����ж����������ͼ���˸���Ĭ�϶����е�ͼ��Ч��
            // ������ע�⣬�˴��ܲ�������Ķ���������LObject��������������Ϸ��ɫ��
            Follow(hero);

            // ������Ծ�¼�
            hero.listener = new JumpI(indexMap, enemyAnimation);
            AddActionKey(Key.LEFT, new GoLeftKey());
            AddActionKey(Key.RIGHT, new GoRightKey());
            AddActionKey(Key.UP, new GoJumpKey());
            if (LSystem.type != LSystem.ApplicationType.JavaSE)
            {

                LPad pad = new LPad(10, 180);
                pad.SetListener(new PadClick(this));
                Add(pad);
            }
            this.updateListener =new GameUpdateListener(this);
        }

        class GameUpdateListener : UpdateListener
        {

            private GameMapTest game;

            public GameUpdateListener(GameMapTest test)
            {
                this.game = test;
            }

            public void Act(SpriteBatchObject sprite, long elapsedTime)
            {

                // ����������ͼ��������������ײ�����·ֱ���֤��
                if (game.hero.IsCollision(sprite))
                {
                    // �����
                    if (sprite is Enemy)
                    {
                        Enemy e = (Enemy)sprite;
                        if (game.hero.Y() < e.Y())
                        {
                            game.hero.SetForceJump(true);
                            game.hero.Jump();
                            game.RemoveTileObject(e);
                        }
                        else
                        {
                            game.Damage();
                        }
                        // ����
                    }
                    else if (sprite is Coin)
                    {
                        Coin coin = (Coin)sprite;
                        game.RemoveTileObject(coin);
                        // ����ٵ���
                    }
                    else if (sprite is Accelerator)
                    {
                        game.RemoveTileObject(sprite);
                        Accelerator accelerator = (Accelerator)sprite;
                        accelerator.Use(game.hero);
                        // ����ε�������
                    }
                    else if (sprite is JumperTwo)
                    {
                        game.RemoveTileObject(sprite);
                        JumperTwo jumperTwo = (JumperTwo)sprite;
                        jumperTwo.Use(game.hero);
                    }
                }
            }
        }


        class PadClick : LPad.ClickListener
        {

            private GameMapTest game;

            public PadClick(GameMapTest test)
            {
                this.game = test;

            }

            public void Up()
            {
                game.PressActionKey(Key.UP);
            }

            public void Right()
            {
                game.PressActionKey(Key.RIGHT);
            }

            public void Left()
            {
                game.PressActionKey(Key.LEFT);
            }

            public void Down()
            {
                game.PressActionKey(Key.DOWN);
            }

            public void Other()
            {
                game.ReleaseActionKeys();
            }

        }


        // ��Ӧ�������ߵļ����¼�
        class GoLeftKey : ActionKey
        {
            public override void Act(long e)
            {
                GameMapTest game = (GameMapTest)StaticCurrentSceen;
                game.hero.SetMirror(true);
                game.hero.AccelerateLeft();
            }
        };

        // ��Ӧ�������ߵļ����¼�
        class GoRightKey : ActionKey
        {
            public override void Act(long e)
            {
                GameMapTest game = (GameMapTest)StaticCurrentSceen;
                game.hero.SetMirror(false);
                game.hero.AccelerateRight();
            }
        };

        // ��Ӧ�������ߵļ����¼�
        class GoJumpKey : ActionKey
        {
            public override void Act(long e)
            {
                GameMapTest game = (GameMapTest)StaticCurrentSceen;
                game.hero.Jump();
            }
        };

        class JumpI : JumpObject.JumpListener
        {

            TileMap indexMap;

            Animation enemyAnimation;

            public JumpI(TileMap indexMap, Animation enemyAnimation)
            {
                this.indexMap = indexMap;
                this.enemyAnimation = enemyAnimation;
            }

            public void Update(long elapsedTime)
            {

            }

            public void Check(int x, int y)
            {
                GameMapTest game = (GameMapTest)StaticCurrentSceen;
                if (indexMap.GetTileID(x, y) == 'C')
                {
                    indexMap.SetTileID(x, y, 'c');
                    Enemy enemy = new Enemy(indexMap.TilesToPixelsX(x),
                            indexMap.TilesToPixelsY(y - 1), new Animation(
                                    enemyAnimation), indexMap);
                    game.Add(enemy);
                    // ��ע��ͼ���࣬ǿ�ƻ���ˢ��
                    indexMap.SetDirty(true);
                }
                else if (indexMap.GetTileID(x + 1, y) == 'C')
                {
                    indexMap.SetTileID(x + 1, y, 'c');
                    indexMap.SetDirty(true);
                }

            }
        }
        
	private RotateTo rotate;

    public void Damage()
    {
        // �����������ײʱ(���ǲȵ��˵���)������һ����ת����(��ʵЧ���������ĸ���ȤһЩ��
        // �����ȷ�����ĳһ����(FireTo)��Ȼ���ٵ��صȵȣ��˴������ٸ�����)
        if (rotate == null)
        {
            // ��ת360�ȣ�ÿ֡�ۼ�5��
            rotate = new RotateTo(360f, 5f);
            rotate.SetActionListener(new RotateActionListener(this));
            AddAction(rotate, hero);
        }
        else if (rotate.IsComplete())
        {
            hero.SetFilterColor(LColor.red);
            // ֱ������rotate����
            rotate.Start(hero);
            // ���²���(LGame�ķ�����Action�¼������ҽ������Զ�ɾ�����¼���������Ҫ���²���)
            AddAction(rotate, hero);
        }
    }

        class RotateActionListener : ActionListener {

                private GameMapTest game;

                public RotateActionListener(GameMapTest test){
                      this.game = test;
                }

				public void Stop(ActionBind o) {
					game.hero.SetFilterColor(LColor.white);
					game.hero.SetRotation(0);
				}

				public void Start(ActionBind o) {
					game.hero.SetFilterColor(LColor.red);
					game.hero.Jump();
				}

				public void Process(ActionBind o) {

				}
			}

        public override void After(SpriteBatch batch)
        {

        }

        public override void Before(SpriteBatch batch)
        {

        }

        public override void Press(Loon.Core.Input.LKey e)
        {

        }

        public override void Release(Loon.Core.Input.LKey e)
        {

        }

        public override void Update(long elapsedTime)
        {
            if (hero != null)
            {
                hero.Stop();
            }
        }

        public override void Close()
        {

        }

        public override void TouchDown(Loon.Core.Input.LTouch e)
        {

        }

        public override void TouchUp(Loon.Core.Input.LTouch e)
        {

        }

        public override void TouchMove(Loon.Core.Input.LTouch e)
        {

        }

        public override void TouchDrag(Loon.Core.Input.LTouch e)
        {

        }
    }
}
