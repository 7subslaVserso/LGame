using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using Loon.Java;
using Loon.Utils;
using Loon.Core.Graphics.OpenGL;
using Loon.Core.Graphics;
using Loon.Utils.Debug;

namespace Loon.Core.Resource
{
    public abstract class LPKResource
    {

        /// <summary>
        /// ����ָ����Դ�ļ��е�ָ����ԴΪLImage
        /// </summary>
        ///
        /// <param name="fileName"></param>
        /// <param name="resName"></param>
        /// <returns></returns>
        public static LTexture OpenTexture(string fileName, string resName)
        {
            try
            {
                Stream stream = OpenStream(fileName, resName);
                LTexture texture = new LTexture(stream);
                return texture;
            }
            catch (Exception ex)
            {
                Log.Exception(ex);
                throw new Exception("File not found. ( " + resName + " )");
            }
        }

        /// <summary>
        /// ����ָ����Դ�ļ��е�ָ����ԴΪLPixmap
        /// </summary>
        ///
        /// <param name="fileName"></param>
        /// <param name="resName"></param>
        /// <returns></returns>
        public static LPixmap OpenPixmap(string fileName, string resName)
        {
            try
            {
                LTexture texture = OpenTexture(fileName,resName);
                return new LPixmap(texture);
            }
            catch (Exception ex)
            {
                Log.Exception(ex);
                throw new Exception("File not found. ( " + resName + " )");
            }
        }

        /// <summary>
        /// ����ָ����Դ���е�ָ����Դ�ļ�������ΪStream
        /// </summary>
        /// <param name="fileName"></param>
        /// <param name="resName"></param>
        /// <returns></returns>
        public static Stream OpenStream(string fileName, string resName)
        {
            byte[] bytes = OpenResource(fileName, resName);
            MemoryStream byteArrayOutputStream = new MemoryStream(bytes);
            return byteArrayOutputStream;
        }

        /// <summary>
        /// ����ָ����Դ���е�ָ����Դ�ļ�������ΪByte[]
        /// </summary>
        ///
        /// <param name="fileName"></param>
        /// <param name="resName"></param>
        /// <returns></returns>
        public static byte[] OpenResource(string fileName, string resName)
        {
     
            Stream ins0 = null;
            DataInputStream dis = null;
            try
            {
                ins0 = Resources.OpenStream(fileName);
                dis = new DataInputStream(ins0);
                LPKHeader header = ReadHeader(dis);
                LPKTable[] fileTable = ReadLPKTable(dis, (int)header.GetTables());
                bool find = false;
                int fileIndex = 0;
                string innerName = null;
                for (int i = 0; i < fileTable.Length; i++)
                {
                    innerName = StringUtils.NewString(fileTable[i].GetFileName()).Trim();
                    if (innerName.Equals(resName,StringComparison.InvariantCultureIgnoreCase))
                    {
                        find = true;
                        fileIndex = i;
                        break;
                    }
                }
                if (find == false)
                {
                    throw new Exception("File not found. ( " + fileName
                            + " )");
                }
                else
                {
                    return ReadFileFromPak(dis, header, fileTable[fileIndex]);
                }
            }
            catch (Exception ex)
            {
                Log.Exception(ex);
                throw new Exception("File not found. ( " + fileName + " )");
            }
            finally
            {
                if (dis != null)
                {
                    try
                    {
                        dis.Close();
                        dis = null;
                    }
                    catch (IOException ex)
                    {
                        Log.Exception(ex);
                    }

                }
            }
        }

        /// <summary>
        /// ����LPK�ļ���Ϣ
        /// </summary>
        ///
        /// <param name="pakFilePath"></param>
        /// <returns></returns>
        /// <exception cref="System.Exception"></exception>
        public static IList<object> GetLPKInfo(string resName)
        {
            Stream ins0 = Resources.OpenStream(resName);
            DataInputStream dis = new DataInputStream(ins0);
            LPKHeader header = ReadHeader(dis);
            LPKTable[] fileTable = ReadLPKTable(dis, (int)header.GetTables());
            List<object> result = new List<object>();
            CollectionUtils.Add(result, header);
            CollectionUtils.Add(result, fileTable);
            return result;
        }

        /// <summary>
        /// ��ȡͷ�ļ�
        /// </summary>
        ///
        /// <param name="dis"></param>
        /// <returns></returns>
        /// <exception cref="System.Exception"></exception>
        public static LPKHeader ReadHeader(DataInputStream dis)
        {
            LPKHeader header = new LPKHeader();
            header.SetPAKIdentity(dis.ReadInt());
            byte[] pass = ReadByteArray(dis, LPKHeader.LF_PASSWORD_LENGTH);
            header.SetPassword(pass);
            header.SetVersion(dis.ReadFloat());
            header.SetTables(dis.ReadLong());
            return header;
        }

        /// <summary>
        /// ��ȡ�ļ��б�
        /// </summary>
        ///
        /// <param name="dis"></param>
        /// <param name="fileTableNumber"></param>
        /// <returns></returns>
        /// <exception cref="System.Exception"></exception>
        public static LPKTable[] ReadLPKTable(DataInputStream dis,
                int fileTableNumber)
        {
            LPKTable[] fileTable = new LPKTable[fileTableNumber];
            for (int i = 0; i < fileTableNumber; i++)
            {
                LPKTable ft = new LPKTable();
                ft.SetFileName(ReadByteArray(dis, LPKHeader.LF_FILE_LENGTH));
                ft.SetFileSize(dis.ReadLong());
                ft.SetOffSet(dis.ReadLong());
                fileTable[i] = ft;
            }
            return fileTable;
        }

        /// <summary>
        /// ��ȡ������
        /// </summary>
        ///
        /// <param name="dis"></param>
        /// <param name="header"></param>
        /// <param name="fileTable"></param>
        /// <returns></returns>
        /// <exception cref="System.Exception"></exception>
        public static byte[] ReadFileFromPak(DataInputStream dis, LPKHeader header,
                LPKTable fileTable)
        {
            dis.Skip(fileTable.GetOffSet() - OutputOffset(header));
            int fileLength = (int)fileTable.GetFileSize();
            byte[] fileBuff = new byte[fileLength];
            int readLength = dis.Read(fileBuff, 0, fileLength);
            if (readLength < fileLength)
            {
                return null;
            }
            else
            {
                MakeBuffer(fileBuff, readLength);
                return fileBuff;
            }
        }

        /// <summary>
        /// ��ȡByte[]
        /// </summary>
        ///
        /// <param name="dis"></param>
        /// <param name="readLength"></param>
        /// <returns></returns>
        /// <exception cref="System.Exception"></exception>
        public static byte[] ReadByteArray(DataInputStream dis, int readLength)
        {
            byte[] readBytes = new byte[readLength];
            for (int i = 0; i < readLength; i++)
            {
                readBytes[i] = (byte)dis.ReadByte();
            }
            return readBytes;
        }

        /// <summary>
        /// ���ָ��ͷ�ļ���ƫ�Ƴ���
        /// </summary>
        ///
        /// <param name="header"></param>
        /// <returns></returns>
        public static long OutputOffset(LPKHeader header)
        {
            return LPKHeader.Size() + header.GetTables() * LPKTable.Size();
        }

        /// <summary>
        /// �ƶ�ƫ��λ��
        /// </summary>
        ///
        /// <param name="sourceFileSize"></param>
        /// <param name="lastFileOffset"></param>
        /// <returns></returns>
        public static long OutputNextOffset(long sourceFileSize, long lastFileOffset)
        {
            return lastFileOffset + sourceFileSize;
        }

        /// <summary>
        /// ��������
        /// </summary>
        ///
        /// <param name="data"></param>
        /// <param name="size"></param>
        public static void MakeBuffer(byte[] data, int size)
        {
            for (int i = 0; i < size; i++)
            {
                data[i] ^= 0xF7;
            }
        }
    }
}
