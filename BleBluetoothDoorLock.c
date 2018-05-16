#include <reg52.h>
unsigned int flage=0,len,i,class,checkFlag,sendPwdflag;
unsigned char sendMsg,receiveData[20]="",pwd[6]="";
unsigned char masterPwd[6]="128128",userPwd[6]="281281",loginPwd[6]="812812";  
sbit led=P2^0;

void clean();
void checkMsg();
void open();
void close();
void openDoor();
void changePwd();
void checkPwd();
void checkMasterPwd();
void checkUserPwd();
void checkLoginPwd();
void delay(unsigned int j);
void checkSendMsg();

void main()  
{  
    SCON=0X50;  //设置为工作方式1
    TMOD=0x20;  //设置定时器1为工作方式2
    PCON=0X80;  //波特率加倍 
                // 波特率=2600时定时初值为F3H  
    TH1=0XF3;   //装载TH1  
    TL1=0XF3;   //装载TL1  
    TR1=1;      //启动T1（定时器1），开始计数  
    REN=1;      //允许串行接收  
                //SM0=0 SM1=1 为工作方式2  
    SM0=0;  
    SM1=1;  
    EA=1;       //开启总中断  
    ES=1;       //允许串口产生中断  
    
    while(1)  
    {  
        if(flage==1)  
        {  
            ES=0;//不允许串口产生中断 保证此次操作安全  
            flage=0;
            checkMsg();  
            SBUF=sendMsg;//发送数据 
            while(!TI);  
            TI=0;//取消此次中断申请 
            checkSendMsg();
            clean();
            ES=1;//允许串口产生中断
        }  
    }  
}  

void delay(unsigned int j)
{
    while(j--);
}  

void checkSendMsg()
{
    if(sendPwdflag)
    {
        for(i=0;i<6;i++)
        {
            SBUF=userPwd[i];
            while(!TI);  
            TI=0;//取消此次中断申请 
        }
        for(i=0;i<6;i++)
        {
            SBUF=loginPwd[i];
            while(!TI);  
            TI=0;//取消此次中断申请 
        }
    }
}

void clean()
{
    for(i=0;i<20;i++)
        receiveData[i]=0;
    for(i=0;i<6;i++)
        pwd[i]=0;
    sendMsg=0x38;
    len=0;
    checkFlag=0;
    sendPwdflag=0;
}

void checkMsg()
{
    if(receiveData[0]==0x01)//验证开锁
    {
        if(len<8)
        {
            sendMsg=0x38;
            return;
        }
        for(i=0;i<6;i++)
            pwd[i]=receiveData[i+1];
        checkPwd();
        openDoor();
    }
    else if(receiveData[0]==0x02)//验证修改
    {
        if(len<15)
        {
            sendMsg=0x38;
            return;
        }
        for(i=0;i<6;i++)
            pwd[i]=receiveData[i+1];
        checkPwd();
        for(i=0;i<6;i++)
            pwd[i]=receiveData[i+8];
        if(class==1)
        {
            class=receiveData[7];
            changePwd();
        }
        else
            sendMsg=0x38;

    }
    else if(receiveData[0]==0x03)//分享秘钥  
    {
        if(len<8)
        {
            sendMsg=0x38;
            return;
        }
        for(i=0;i<6;i++)
            pwd[i]=receiveData[i+1];
        checkPwd();
        if(class==1)
        {
            sendMsg=0x32;
            sendPwdflag=1;
            return;
        }
    }
    else if(receiveData[0]==0x00)//直接关闭  
    {
        close();
        sendMsg=0x30;
    }
}

void open()
{
    led=0;
    delay(50000);
    close();
}

void close()
{
    led=1;
}

void openDoor()
{
    if(class==1)//开,主人
    {
        sendMsg=0x31;
        open();
    }
    else if(class==2)//开,用户  
    {
        sendMsg=0x32;
        open();
    }
    else if(class==3)//开,访客
    {

        sendMsg=0x33;
        open();
    }
    else
    {
        sendMsg=0x38;
    }
}

void changePwd()
{
    if(class==1)//主人
    {
        for(i=0;i<6;i++)
            masterPwd[i]=pwd[i];
        sendMsg=0x31;
    }
    else if(class==2)//用户  
    {
        for(i=0;i<6;i++)
            userPwd[i]=pwd[i];
        sendMsg=0x32;
    }
    else if(class==3)//访客
    {
        for(i=0;i<6;i++)
            loginPwd[i]=pwd[i];
        sendMsg=0x33;
    }
    else
    {
        sendMsg=0x38;
    }   
}

void checkMasterPwd()
{
    for(i=0;i<6;i++)
    {
        if(pwd[i]!=masterPwd[i])
        {
            checkFlag=0;
            return;
        }
    }
    checkFlag=1;
}

void checkUserPwd()
{
    for(i=0;i<6;i++)
    {
        if(pwd[i]!=userPwd[i])
        {
            checkFlag=0;
            return;
        }
    }
    checkFlag=1;
}

void checkLoginPwd()
{
    for(i=0;i<6;i++)
    {
        if(pwd[i]!=loginPwd[i])
        {
            checkFlag=0;
            return;
        }
    }
    checkFlag=1;
}

void checkPwd()
{
    checkMasterPwd();
    if(checkFlag)//主人
    {
        class=1;
        return;
    }
    checkUserPwd();
    if(checkFlag)//用户
    {
        class=2;
        return;
    } 
    checkLoginPwd(); 
    if(checkFlag)//访客
    {
        class=3;
        return;
    }
    class=0;
}


void interrupt4() interrupt 4//4号中断 接收数据  
{
    if(RI)
    {
        receiveData[len]=SBUF;
        len++;
        if(len>18)
            clean();
        if(SBUF=='#')
            flage=1;
        RI=0;//取消此次中断申请   
    }
}