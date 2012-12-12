namespace Loon.Action.Avg.Drama
{
    using System;
    using Loon.Core;

    public abstract class Expression
    {

        // Ĭ�ϱ���1,���ڼ�¼��ǰѡ����
        public const string V_SELECT_KEY = "SELECT";

        // ������
        public const string BRACKET_LEFT_TAG = "(";

        // ������
        public const string BRACKET_RIGHT_TAG = ")";

        // ����ο�ʼ���
        public const string BEGIN_TAG = "begin";

        // ����ν������
        public const string END_TAG = "end";

        // ����ε��ñ��
        public const string CALL_TAG = "call";

        // ����ˢ�±��
        public const string RESET_CACHE_TAG = "Reset";

        // �ۼ��������ݱ��
        public const string IN_TAG = "in";

        // �ۼ���������ֹͣ����������
        public const string OUT_TAG = "out";

        // ��ѡ���
        public const string SELECTS_TAG = "selects";

        // ��ӡ���
        public const string PRINT_TAG = "print";

        // ��������
        public const string RAND_TAG = "rand";

        // �趨�����������
        public const string SET_TAG = "set";

        // �����ڲ��ű����
        public const string INCLUDE_TAG = "include";

        // �����ж����
        public const string IF_TAG = "if";

        // �����ж��������
        public const string IF_END_TAG = "endif";

        // ת�۱��
        public const string ELSE_TAG = "else";

        // ����Ϊע�ӷ���
        public const string FLAG_L_TAG = "//";

        public const string FLAG_C_TAG = "#";

        public const string FLAG_I_TAG = "'";

        public const string FLAG_LS_B_TAG = "/*";

        public const string FLAG_LS_E_TAG = "*/";

        public const string FLAG = "@";

        public const string FLAG_SAVE_TAG = "save";

        public const string FLAG_LOAD_TAG = "load";

        public const char FLAG_CHAR = '@';

    }
}
