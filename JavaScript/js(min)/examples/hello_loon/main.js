//���к��������Զ������������ã������û��ֶ�����
$package('loon.main');

/**
 * loon��ں���
 */
function OnMain(){
	//������Ϸ��ʼ������
    var setting = new Setting();
        setting.fps = 30;
	    setting.showFps = true;

       //ע����Ϸ����
       register(setting);
};
	
/**
 * ��Ϸ�����̣���Ϸ�����������ó�ʼ��ִ��
 */
function OnProcess(/*loon.Core*/core){		

          //�����߳��ύһ����Ϸ�¼�
		  var event = {
			run:(function(e){alert("Hello Loon!")}),
		  };

		  core.post(event);
}

/**
 * ��Ϸ����ˢ��ʱ�����ô˺���(�ֶ�API��Ⱦ��,���useCanvasUpdateΪfalse����Ч)
 */
function OnUpdate(/*loon.Renderer*/render,/*float*/elapsed){
         render.drawText("loon-min-0.3.3",context().width/2,context().height/2);
}
