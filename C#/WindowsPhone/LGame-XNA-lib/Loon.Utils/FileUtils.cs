using System;
using System.IO;
using System.Collections.Generic;
using System.Text;
using System.IO.IsolatedStorage;
using Loon.Java;
using Loon.Core;

namespace Loon.Utils
{
    public sealed class FileUtils
    {

        public static DataInputStream ReadIsolatedStorageFileToDataInput(string path)
        {
            return ReadIsolatedStorageFileToDataInput(IsolatedStorageFile.GetUserStoreForApplication(), path);
        }

        public static DataOutputStream WriteIsolatedStorageFileToDataInput(string path)
        {
            return WriteIsolatedStorageFileToDataInput(IsolatedStorageFile.GetUserStoreForApplication(), path);
        }

        public static DataInputStream ReadIsolatedStorageFileToDataInput(IsolatedStorageFile isoStorage,string path)
        {
            IsolatedStorageFileStream stream = isoStorage.OpenFile(path, FileMode.Open);
            return new DataInputStream(stream);
        }

        public static DataOutputStream WriteIsolatedStorageFileToDataInput(IsolatedStorageFile isoStorage, string path)
        {
            IsolatedStorageFileStream stream = isoStorage.OpenFile(path, FileMode.OpenOrCreate);
            return new DataOutputStream(stream);
        }

        public static Stream ReadIsolatedStorageFileToStream(IsolatedStorageFile isoStorage, string path)
        {
            IsolatedStorageFileStream stream = isoStorage.OpenFile(path, FileMode.Open);
            return stream;
        }

        public static Stream WriteIsolatedStorageFileToStream(IsolatedStorageFile isoStorage, string path)
        {
            IsolatedStorageFileStream stream = isoStorage.OpenFile(path, FileMode.OpenOrCreate);
            return stream;
        }

        /// <summary>
        /// ��IsolatedStorage���ļ�д������
        /// </summary>
        /// 
        /// <param name="path"></param>
        /// <param name="records"></param>
        public static void WriteIsolatedStorageFile(string path, List<string> records)
        {
            WriteIsolatedStorageFile(path, records, true);
        }

        /// <summary>
        /// ��IsolatedStorage���ļ�д������
        /// </summary>
        /// 
        /// <param name="path"></param>
        /// <param name="records"></param>
        /// <param name="append"></param>
        public static void WriteIsolatedStorageFile(string path, List<string> records,
        bool append)
        {
            WriteIsolatedStorageFile(path, records, append, System.Text.Encoding.UTF8);
        }

        /// <summary>
        /// ��IsolatedStorage���ļ�д������
        /// </summary>
        /// 
        /// <param name="path"></param>
        /// <param name="records"></param>
        /// <param name="append"></param>
        /// <param name="encoding"></param>
        public static void WriteIsolatedStorageFile(string path, List<string> records,
        bool append, System.Text.Encoding encoding)
        {
            IsolatedStorageFile isoStorage = IsolatedStorageFile.GetUserStoreForApplication();
            if (!isoStorage.FileExists(path))
            {
                isoStorage.CreateFile(path);
            }
            IsolatedStorageFileStream fileStream = null;
            try
            {
                fileStream = isoStorage.OpenFile(path, append ? FileMode.Append : FileMode.CreateNew);
                foreach (string s in records)
                {
                    byte[] buffer = encoding.GetBytes(s);
                    fileStream.Write(buffer, 0, buffer.Length);
                }
            }
            finally
            {
                if (fileStream != null)
                {
                    fileStream.Close();
                    fileStream = null;
                }
            }
        }

        /// <summary>
        /// ���ָ����׺��IsolatedStorage�ļ�����
        /// </summary>
        /// 
        /// <param name="ext"></param>
        /// <returns></returns>
        public static List<string> GetIsolatedStorageFiles(string ext)
        {
            IsolatedStorageFile isoStorage = IsolatedStorageFile.GetUserStoreForApplication();
            List<string> ret = new List<string>();
            string[] listFile = isoStorage.GetFileNames();
            if (listFile != null)
            {
                for (int i = 0; i < listFile.Length; i++)
                {
                        string name = listFile[i];

                        if (GetExtension(name).Equals(ext, StringComparison.InvariantCultureIgnoreCase))
                        {
                            ret.Add(name);
                        }
                }
            }
            return ret;
        }

        /// <summary>
        /// ��õ�ǰIsolatedStorage�ļ�����
        /// </summary>
        /// 
        /// <returns></returns>
        public static List<string> GetIsolatedStorageFiles()
        {
            IsolatedStorageFile isoStorage = IsolatedStorageFile.GetUserStoreForApplication();
            List<string> ret = new List<string>();
            string[] listFile = isoStorage.GetFileNames();
            if (listFile != null)
            {
                for (int i = 0; i < listFile.Length; i++)
                {
                    ret.Add(listFile[i]);
                }
            }
            return ret;
        }

        /// <summary>
        /// ����һ���յ�IsolatedStorageĿ¼
        /// </summary>
        /// 
        /// <param name="directoryName"></param>
        public static void CreateIsolatedStorageDirectory(string directoryName)
        {
            try
            {
                IsolatedStorageFile isoStorage = IsolatedStorageFile.GetUserStoreForApplication();
                if (!string.IsNullOrEmpty(directoryName) && !isoStorage.DirectoryExists(directoryName))
                {
                    isoStorage.CreateDirectory(directoryName);
                }
            }
            catch 
            {
            
            }
        }

        /// <summary>
        /// ����һ���յ�IsolatedStorage�ļ�
        /// </summary>
        /// 
        /// <param name="resName"></param>
        public static void CreateIsolatedStorageFile(string resName)
        {
            try
            {
                IsolatedStorageFile isoStorage = IsolatedStorageFile.GetUserStoreForApplication();
                if (!string.IsNullOrEmpty(resName) && !isoStorage.FileExists(resName))
                {
                    isoStorage.CreateFile(resName);
                }
            }
            catch
            {

            }
        }

        /// <summary>
        /// ɾ��һ��ָ����IsolatedStorageĿ¼
        /// </summary>
        /// 
        /// <param name="directoryName"></param>
        public static void DeleteIsolatedStorageDirectory(string directoryName)
        {
            try
            {
                IsolatedStorageFile isoStorage = IsolatedStorageFile.GetUserStoreForApplication();
                if (!string.IsNullOrEmpty(directoryName) && isoStorage.DirectoryExists(directoryName))
                {
                    isoStorage.DeleteDirectory(directoryName);
                }
            }
            catch 
            {

            }
        }

        /// <summary>
        /// ɾ��һ��ָ����IsolatedStorage�ļ�
        /// </summary>
        /// <param name="resName"></param>
        public static void DeleteIsolatedStorageFile(string resName)
        {
            try
            {
                IsolatedStorageFile isoStorage = IsolatedStorageFile.GetUserStoreForApplication();
                if (!string.IsNullOrEmpty(resName) && isoStorage.FileExists(resName))
                {
                    isoStorage.DeleteFile(resName);
                }
            }
            catch
            {

            }
        }

        /// <summary>
        /// ɾ��IsolatedStorage������Ŀ¼
        /// </summary>
        /// 
        /// <returns></returns>
        public static bool DeleteIsolatedStorageDirectory()
        {
            IsolatedStorageFile isoStorage = IsolatedStorageFile.GetUserStoreForApplication();
            string[] listFile = isoStorage.GetDirectoryNames();
            if (listFile != null)
            {
                for (int i = 0; i < listFile.Length; i++)
                {
                    if (isoStorage.DirectoryExists(listFile[i]))
                    {
                        isoStorage.DeleteDirectory(listFile[i]);
                    }
                }
            }
            else
            {
                return false;
            }
            return true;
        }

        /// <summary>
        /// ɾ��IsolatedStorage�������ļ�
        /// </summary>
        /// 
        /// <param name="resName"></param>
        /// <returns></returns>
        public static bool DeleteIsolatedStorageFile()
        {
            IsolatedStorageFile isoStorage = IsolatedStorageFile.GetUserStoreForApplication();
            string[] listFile = isoStorage.GetFileNames();
            if (listFile != null)
            {
                for (int i = 0; i < listFile.Length; i++)
                {
                    if (isoStorage.FileExists(listFile[i]))
                    {
                        isoStorage.DeleteFile(listFile[i]);
                    }
                }
            }
            else
            {
                return false;
            }
            return true;
        }

        /// <summary>
        /// �ж�ָ����IsolatedStorage�ļ��Ƿ����
        /// </summary>
        /// 
        /// <param name="resName"></param>
        /// <returns></returns>
        public static bool ExistsIsolatedStorage(string resName)
        {
            IsolatedStorageFile isoStorage = IsolatedStorageFile.GetUserStoreForApplication();
            return isoStorage.FileExists(resName);
        }

        /// <summary>
        /// ��ȡIsolatedStorage����Ϊ�ַ�����Ϣ
        /// </summary>
        /// 
        /// <param name="filePath"></param>
        /// <returns></returns>
        public static string ReadIsolatedStorageFileToString(string filePath)
        {
            IsolatedStorageFile store = IsolatedStorageFile.GetUserStoreForApplication();
            IsolatedStorageFileStream stream = null;
            string data;
            try
            {
                stream = store.OpenFile(filePath, FileMode.Open);
                byte[] buffer = new byte[stream.Length];
                stream.Read(buffer, 0, buffer.Length);
                data = Encoding.UTF8.GetString(buffer, 0, buffer.Length);
            }
            catch (Exception e)
            {
                throw e;
            }
            finally
            {
                if (stream != null)
                {
                    stream.Close();
                    stream = null;
                }
            }
            return data;
        }

        /// <summary>
        ///  ��ȡIsolatedStorage����Ϊ������
        /// </summary>
        /// 
        /// <param name="resName"></param>
        /// <param name="write"></param>
        /// <returns></returns>
        private static IsolatedStorageFileStream ReadIsolatedStorageFileStream(string resName, bool write)
        {
            IsolatedStorageFile isf = IsolatedStorageFile.GetUserStoreForApplication();
            if (write)
            {
                return new IsolatedStorageFileStream(resName, FileMode.Create, isf);
            }
            else
            {
                return new IsolatedStorageFileStream(resName, FileMode.OpenOrCreate, isf);
            }
        }


        /// <summary>
        /// ������������IsolatedStorage����Ϊָ���ļ�
        /// </summary>
        /// 
        /// <param name="stream"></param>
        /// <param name="cachePath"></param>
        public static void SaveStreamToIsolatedStorageFile(Stream stream, string cachePath)
        {
            IsolatedStorageFileStream fileStream = null;
            try
            {
                byte[] buffer = new byte[stream.Length];
                IsolatedStorageFile store = IsolatedStorageFile.GetUserStoreForApplication();
                if (store.FileExists(cachePath))
                {
                    store.DeleteFile(cachePath);
                }
                if (!store.FileExists(cachePath))
                {
                    fileStream = store.OpenFile(cachePath, FileMode.OpenOrCreate);
                    stream.Read(buffer, 0, buffer.Length);
                    fileStream.Write(buffer, 0, buffer.Length);
                }
            }
            finally
            {
                if (stream != null)
                {
                    stream.Close();
                    stream = null;
                }
                if (fileStream != null)
                {
                    fileStream.Close();
                    fileStream = null;
                }
            }
        }

        /// <summary>
        /// ���ָ����IsolatedStorage�ļ���С
        /// </summary>
        /// 
        /// <param name="filePath"></param>
        /// <returns></returns>
        public static long GetIsolatedStorageFileSize(string filePath)
        {
            using (IsolatedStorageFile storageFile = IsolatedStorageFile.GetUserStoreForApplication())
            {
                using (IsolatedStorageFileStream fileStream = storageFile.OpenFile(filePath, FileMode.Open, FileAccess.Read, FileShare.ReadWrite))
                {
                    return fileStream.Length;
                }
            }
        }


        /// <summary>
        /// ���е���ַ�ڷ�б��
        /// </summary>
        ///
        /// <param name="str"></param>
        /// <returns></returns>
        public static string CutFileName(string str)
        {
            if (str == null)
                return null;
            str = CutDC(str);
            int idx;
            idx = str.LastIndexOf("\\");
            if ((idx + 1) < str.Length)
            {
                str = str.Substring(idx + 1);
            }
            idx = str.LastIndexOf("/");
            if ((idx + 1) < str.Length)
            {
                str = str.Substring(idx + 1);
            }
            return str;
        }

        /// <summary>
        /// ���з�б��
        /// </summary>
        ///
        /// <param name="str"></param>
        /// <returns></returns>
        private static string CutDC(string str)
        {
            if (str == null)
                return null;
            if (str.Equals("\"\""))
                return "";
            if (str.Length > 1)
            {
                if (str.Substring(0, (1) - (0)).Equals("\""))
                {
                    str = str.Substring(1);
                }
            }
            if (str.Length > 1)
            {
                if (str.Substring(str.Length - 1).Equals("\""))
                {
                    str = str.Substring(0, (str.Length - 1) - (0));
                }
            }
            return str;
        }

        /// <summary>
        /// ɾ��ָ�����Ƶ��ļ�
        /// </summary>
        /// 
        /// <param name="resName"></param>
        public void Delete(string resName)
        {
            #if WPF
                     if (File.Exists(resName))
                     {
                         File.Delete(resName);
                     }
            #else
                     if (IsolatedStorageFile.GetUserStoreForApplication().FileExists(resName))
                     {
                         IsolatedStorageFile.GetUserStoreForApplication().DeleteFile(resName);
                     }
            #endif
        }

        /// <summary>
        /// �ر�ָ������
        /// </summary>
        ///
        /// <param name="input"></param>
        /// <param name="file"></param>
        public static void Close(Stream ins)
        {
            if (ins != null)
            {
                try
                {
                    ins.Close();
                }
                catch (IOException e)
                {
                    ClosingFailed(e);
                }
            }
        }

        /// <summary>
        /// �ر�ָ������
        /// </summary>
        ///
        /// <param name="reader"></param>
        /// <param name="file"></param>
        public static void Close(TextReader reader)
        {
            if (reader != null)
            {
                try
                {
                    reader.Close();
                }
                catch (IOException e)
                {
                    ClosingFailed(e);
                }
            }
        }

        /// <summary>
        /// �ر�ָ������
        /// </summary>
        ///
        /// <param name="writer"></param>
        /// <param name="file"></param>
        public static void Close(TextWriter writer)
        {
            if (writer != null)
            {
                try
                {
                    writer.Close();
                }
                catch (IOException e)
                {
                    ClosingFailed(e);
                }
            }
        }

        /// <summary>
        /// �ر�ָ����������쳣
        /// </summary>
        ///
        /// <param name="file"></param>
        /// <param name="e"></param>
        public static void ClosingFailed(IOException e)
        {
            throw new Exception(e.Message);
        }

        /// <summary>
        /// ����ָ������������
        /// </summary>
        ///
        /// <param name="is"></param>
        /// <param name="os"></param>
        /// <param name="len"></param>
        /// <returns></returns>
        /// <exception cref="IOException"></exception>
        public static long Copy(Stream mask0, Stream os, long len)
        {
            byte[] buf = new byte[1024];
            long copied = 0;
            int read;
            while ((read = mask0.Read(buf, 0, buf.Length)) != 0 && copied < len)
            {
                long leftToCopy = len - copied;
                int toWrite = (read < leftToCopy) ? read : (int)leftToCopy;
                os.Write(buf, 0, toWrite);
                copied += toWrite;
            }
            return copied;
        }

        /// <summary>
        /// ����ָ��������
        /// </summary>
        ///
        /// <param name="in"></param>
        /// <param name="out"></param>
        /// <returns></returns>
        /// <exception cref="IOException"></exception>
        public static long Copy(Stream ins0, Stream xout)
        {
            long written = 0;
            byte[] buffer = new byte[4096];
            while (true)
            {
                int len = ins0.Read(buffer, 0, buffer.Length);
                if (len < 0)
                {
                    break;
                }
                xout.Write(buffer, 0, len);
                written += len;
            }
            return written;
        }


        /// <summary>
        /// ��ȡָ������������
        /// </summary>
        ///
        /// <param name="is"></param>
        /// <param name="len"></param>
        /// <returns></returns>
        /// <exception cref="IOException"></exception>
        public static byte[] Read(Stream mask, long len)
        {
            MemoryStream xout = new MemoryStream();
            Copy(mask, xout, len);
            return xout.ToArray();
        }

        /// <summary>
        /// ƴװ�ļ���
        /// </summary>
        ///
        /// <param name="name"></param>
        /// <param name="ext"></param>
        /// <returns></returns>
        public static string GetName(string name, string ext)
        {
            return string.Intern((name + "." + ext));
        }

        /// <summary>
        /// �����ļ���չ��
        /// </summary>
        ///
        /// <param name="name"></param>
        /// <returns></returns>
        public static string GetNoExtensionName(string name)
        {
            if (name.IndexOf(".") == -1)
                return name;
            else
                return name.Substring(0, (name.LastIndexOf(GetExtension(name)) - 1) - (0));
        }

        /// <summary>
        /// ���ָ���ļ���С
        /// </summary>
        ///
        /// <param name="file"></param>
        /// <returns></returns>
        public static long GetKB(FileInfo file)
        {
            return GetKB(file.Length);
        }

        /// <summary>
        /// ��ָ������ת��ΪKB��ʾ
        /// </summary>
        ///
        /// <param name="size"></param>
        /// <returns></returns>
        public static long GetKB(long size)
        {
            size /= 1000L;
            if (size == 0x0L)
            {
                size = 1L;
            }
            return size;
        }

        /// <summary>
        /// ɾ��ָ��Ŀ¼��ȫ���ļ�
        /// </summary>
        ///
        /// <param name="dir"></param>
        /// <returns></returns>
        public static bool DeleteAll(DirectoryInfo dir)
        {
            string[] fileNames = ToList(dir);
            if (fileNames == null)
                return false;
            for (int i = 0; i < fileNames.Length; i++)
            {
                FileInfo file = new FileInfo(System.IO.Path.Combine(dir.FullName, fileNames[i]));
                if (file.Exists)
                    file.Delete();
                else if (System.IO.Directory.Exists(file.DirectoryName))
                    DeleteAll(file.Directory);
            }
            dir.Delete();
            return true;
        }


        /// <summary>
        /// ��ȡfile
        /// </summary>
        ///
        /// <param name="file"></param>
        /// <returns></returns>
        public static Stream Read(FileInfo file)
        {
            try
            {
                return file.OpenRead();
            }
            catch (FileNotFoundException ex)
            {
                Loon.Utils.Debugging.Log.Exception(ex);
                return null;
            }
        }

        /// <summary>
        /// ��ָ��ȫ·������ȡ�ļ�
        /// </summary>
        ///
        /// <param name="fileName"></param>
        /// <returns></returns>
        public static Stream Read(string fileName)
        {
            return Read(new FileInfo(fileName));
        }

        public static void Copy(TextReader from, TextWriter to)
        {
            char[] buffer = new char[8192];
            int charsRead;
            while ((charsRead = from.Read(buffer, 0, buffer.Length)) != -1)
            {
                to.Write(buffer, 0, charsRead);
                to.Flush();
            }
        }

        /// <summary>
        /// ���ָ��·��Ŀ¼�б�
        /// </summary>
        ///
        /// <param name="path"></param>
        /// <returns></returns>
        public static string[] ToDirList(FileInfo path)
        {
            string pathName = System.IO.Path.GetFullPath(path.Name);
            string[] fileList;
            if ("".Equals(pathName))
                path = new FileInfo(".");
            else
                path = new FileInfo(pathName);
            // ���Ŀ¼�ļ��б�
            if (System.IO.Directory.Exists(path.DirectoryName))
                fileList = ToList(path.Directory);
            else
                return null;
            return fileList;
        }

        /// <summary>
        /// ����ļ��Ƿ����
        /// </summary>
        ///
        /// <param name="file"></param>
        /// <exception cref="IOException"></exception>
        private static void CheckFile(FileInfo file)
        {
            bool exists = file.Exists;
            if (exists && System.IO.Directory.Exists(file.FullName))
            {
                throw new IOException("File " + System.IO.Path.GetFullPath(file.Name)
                        + " is actually not a file.");
            }
        }

        /// <summary>
        /// ���ָ��·��Ŀ¼�б�
        /// </summary>
        ///
        /// <param name="path"></param>
        /// <returns></returns>
        public static string[] ToDirList(string pathName)
        {
            return ToDirList(new FileInfo(pathName));
        }

        /// <summary>
        /// ���ָ��·���µ������ļ���(����ȫ·��)
        /// </summary>
        ///
        /// <param name="path">string ָ��Ŀ¼</param>
        /// <returns>ArrayList �����ļ���(����ȫ·��)</returns>
        /// <exception cref="IOException"></exception>
        public static List<string> GetAllFiles(string path)
        {
            DirectoryInfo file = new DirectoryInfo(path);
            List<string> ret = new List<string>();
            string[] listFile = ToList(file);
            if (listFile != null)
            {
                for (int i = 0; i < listFile.Length; i++)
                {
                    FileInfo tempfile = new FileInfo(path + "/" + listFile[i]);
                    if (System.IO.Directory.Exists(tempfile.DirectoryName))
                    {
                        List<string> arr = GetAllFiles(System.IO.Path.GetFullPath(tempfile.Name));
                        ret.AddRange(arr);
                        arr.Clear();
                        arr = null;
                    }
                    else
                    {
                        ret.Add(tempfile.FullName);

                    }
                }
            }
            return ret;
        }

        /// <summary>
        /// ���ָ��·���µ�����Ŀ¼(����ȫ·��)
        /// </summary>
        ///
        /// <param name="path">string ָ��Ŀ¼</param>
        /// <returns>ArrayList ����Ŀ¼(����ȫ·��)</returns>
        /// <exception cref="IOException"></exception>
        public static List<string> GetAllDir(string path)
        {
            DirectoryInfo file = new DirectoryInfo(path);
            List<string> ret = new List<string>();
            string[] listFile = ToList(file);
            if (listFile != null)
            {
                for (int i = 0; i < listFile.Length; i++)
                {
                    FileInfo tempfile = new FileInfo(path + "/" + listFile[i]);
                    if (System.IO.Directory.Exists(tempfile.DirectoryName))
                    {
                        ret.Add(tempfile.FullName);
                        List<string> arr = GetAllDir(System.IO.Path.GetFullPath(tempfile.Name));
                        ret.AddRange(arr);
                        arr.Clear();
                        arr = null;

                    }
                }
            }
            return ret;

        }

        /// <summary>
        /// ���ָ��·����ָ����չ���������ļ�(����ȫ·��)
        /// </summary>
        ///
        /// <param name="path">string ָ��·��</param>
        /// <param name="ext">string ��չ��</param>
        /// <returns>ArrayList �����ļ�(����ȫ·��)</returns>
        /// <exception cref="IOException"></exception>
        public static List<string> GetAllFiles(string path, string ext)
        {
            DirectoryInfo file = new DirectoryInfo(path);
            List<string> ret = new List<string>();
            string[] exts = StringUtils.Split(ext, ",");
            string[] listFile = ToList(file);
            if (listFile != null)
            {
                for (int i = 0; i < listFile.Length; i++)
                {
                    FileInfo tempfile = new FileInfo(path + "/" + listFile[i]);
                    if (System.IO.Directory.Exists(tempfile.DirectoryName))
                    {
                        List<string> arr = GetAllFiles(System.IO.Path.GetFullPath(tempfile.Name), ext);
                        ret.AddRange(arr);
                        arr.Clear();
                        arr = null;
                    }
                    else
                    {
                        for (int j = 0; j < exts.Length; j++)
                        {
                            if (GetExtension(tempfile.FullName).Equals(exts[j], StringComparison.InvariantCultureIgnoreCase))
                            {
                                ret.Add(tempfile.FullName);
                            }
                        }
                    }
                }
            }
            return ret;
        }

        /// <summary>
        /// ���ָ��·���µ��ļ��б�(����ȫ·��),������һ��Ŀ¼
        /// </summary>
        ///
        /// <param name="path">string ָ��·��</param>
        /// <returns>ArrayList �ļ���</returns>
        /// <exception cref="IOException"></exception>
        public static List<string> GetFiles(string path)
        {
            DirectoryInfo file = new DirectoryInfo(path);
            List<string> ret = new List<string>();
            string[] listFile = ToList(file);
            if (listFile != null)
            {
                for (int i = 0; i < listFile.Length; i++)
                {
                    FileInfo tempfile = new FileInfo(path + "/" + listFile[i]);

                    if (!System.IO.Directory.Exists(tempfile.DirectoryName))
                    {
                        ret.Add(tempfile.FullName);

                    }

                }
            }
            return ret;
        }

        /// <summary>
        /// ���ָ��·���µ���Ŀ¼(����ȫ·��),������һ��Ŀ¼
        /// </summary>
        ///
        /// <param name="path">string ָ��·��</param>
        /// <returns>ArrayList ��Ŀ¼</returns>
        /// <exception cref="IOException"></exception>
        public static List<string> GetDir(string path)
        {
            DirectoryInfo file = new DirectoryInfo(path);
            List<string> ret = new List<string>();
            string[] listFile = ToList(file);
            if (listFile != null)
            {
                for (int i = 0; i < listFile.Length; i++)
                {
                    FileInfo tempfile = new FileInfo(path + "/" + listFile[i]);

                    if (System.IO.Directory.Exists(tempfile.DirectoryName))
                    {
                        ret.Add(tempfile.FullName);

                    }
                }
            }
            return ret;
        }

        /// <summary>
        /// ���ָ��·����ָ����չ�����ļ�(����ȫ·��),������һ��Ŀ¼
        /// </summary>
        ///
        /// <param name="path">string ָ��·��</param>
        /// <param name="ext">string ��չ��</param>
        /// <returns>ArrayList �ļ���</returns>
        /// <exception cref="IOException"></exception>
        public static List<string> GetFiles(string path, string ext)
        {
            DirectoryInfo file = new DirectoryInfo(path);
            List<string> ret = new List<string>();
            string[] listFile = ToList(file);
            if (listFile != null)
            {
                for (int i = 0; i < listFile.Length; i++)
                {
                    FileInfo tempfile = new FileInfo(path + "/" + listFile[i]);

                    if (!System.IO.Directory.Exists(tempfile.DirectoryName))
                    {
                        if (GetExtension(tempfile.FullName).Equals(ext, StringComparison.InvariantCultureIgnoreCase))
                        {
                            ret.Add(tempfile.FullName);
                        }

                    }
                }
            }
            return ret;
        }

        /// <summary>
        /// ���ָ���ļ����Ƿ����
        /// </summary>
        /// 
        /// <param name="directoryToTest"></param>
        /// <returns></returns>
        public static bool DirectoryExists(string directoryToTest)
        {
            return System.IO.Directory.Exists(directoryToTest);
        }

        /// <summary>
        /// ���ָ���ļ��Ƿ����
        /// </summary>
        /// 
        /// <param name="directoryToTest"></param>
        /// <returns></returns>
        public static bool FileExists(string directoryToTest)
        {
            return System.IO.File.Exists(directoryToTest);
        }

        /// <summary>
        /// �����ļ���
        /// </summary>
        /// 
        /// <param name="directoryToCreate"></param>
        public static void CreateDirectory(string directoryToCreate)
        {
            System.IO.Directory.CreateDirectory(directoryToCreate);
        }


        /// <summary>
        /// ���ָ��·����Ŀ¼��
        /// </summary>
        ///
        /// <param name="name"></param>
        /// <returns></returns>
        public static string GetFileName(string name)
        {
            if (name == null)
            {
                return "";
            }
            int length = name.Length;
            int size = name.LastIndexOf("/") + 1;
            if (size < length)
            {
                return name.Substring(size, (length) - (size));
            }
            else
            {
                return "";
            }
        }

        /// <summary>
        /// ���ָ���ļ���չ��
        /// </summary>
        ///
        /// <param name="name"></param>
        /// <returns></returns>
        public static string GetExtension(string name)
        {
            if (name == null)
            {
                return "";
            }
            int index = name.LastIndexOf(".");
            if (index == -1)
            {
                return "";
            }
            else
            {
                return name.Substring(index + 1);
            }
        }


        /// <summary>
        /// ɾ��ָ��Ŀ¼�µ������ļ�
        /// </summary>
        ///
        /// <param name="path">string ָ��Ŀ¼</param>
        /// <exception cref="System.Exception"></exception>
        public static void DeleteFile(string path)
        {
            DirectoryInfo file = new DirectoryInfo(path);
            string[] listFile = ToList(file);
            if (listFile != null)
            {
                for (int i = 0; i < listFile.Length; i++)
                {
                    FileInfo tempfile = new FileInfo(path + "/" + listFile[i]);
                    // �����Ŀ¼
                    if (DirectoryExists(tempfile.DirectoryName))
                    {
                        DeleteFile(System.IO.Path.GetFullPath(tempfile.Name));
                    }
                    else
                    { // �������
                        tempfile.Delete();

                    }
                }
            }
        }

        /// <summary>
        /// ɾ��ָ��Ŀ¼�µ�����Ŀ¼
        /// </summary>
        ///
        /// <param name="path">string ָ��Ŀ¼</param>
        /// <exception cref="System.Exception"></exception>
        public static void DeleteDir(string path)
        {
            DirectoryInfo file = new DirectoryInfo(path);
            string[] listFile = ToList(file);
            if (listFile != null)
            {
                for (int i = 0; i < listFile.Length; i++)
                {
                    FileInfo tempfile = new FileInfo(path + "/" + listFile[i]);
                    if (DirectoryExists(tempfile.DirectoryName))
                    {
                        DeleteDir(System.IO.Path.GetFullPath(tempfile.Name));
                        tempfile.Delete();
                    }
                }
            }

        }

        private static string[] ToList(DirectoryInfo file)
        {
            FileInfo[] files = file.GetFiles();
            int size = files.Length;
            List<string> lists = new List<string>(size);
            for (int i = 0; i < size; i++)
            {
                lists.Add(files[i].Name);
            }
            return lists.ToArray();
        }

    }
}
