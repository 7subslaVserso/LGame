using Loon;
using Loon.Action.Sprite;
namespace PhysicsTest
{
    public class PTest : SpriteBatchScreen
    {

        public override void Create()
        {
            //�����Դ���������
            SetPhysics(true);
            //���һ��ͼ��ΪĬ���������(fixΪtrue������������Ϊ����)
            AddPhysics(false, new TextureObject(166, 66, "a4"));
            // ���������������������籾��
            // GetPhysicsManager().GetWorld();
            // �����������練Ӧ�ٶ�
            SetTimeStep(1 / 30F);
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
        
        }

        public override void Close()
        {
    
        }

        public override void TouchDown(Loon.Core.Input.LTouch e)
        {
            if (IsPhysics())
            {
                SpriteBatchObject o = FindObject(e.GetX(), e.GetY());
                if (o == null)
                {
                    AddCirclePhysics(false, new TextureObject(e.X(), e.Y(),
                            "ball"));
                }
                else
                {
                    Remove(o);
                }
            }
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

    public class Game1 : LGame
    {

        public override void OnMain()
        {
            LSetting setting = new LSetting();
            setting.showFPS = true;
            setting.landscape = true;
            Register(setting, typeof(PTest));
        }

        public override void OnGameResumed()
        {

        }

        public override void OnGamePaused()
        {

        }
    }
}
