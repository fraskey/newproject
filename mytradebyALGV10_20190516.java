//+------------------------------------------------------------------+
//|                                       AutoTradeV10.mq4  |
//|                   Copyright 2005-2018, Copyright. Personal Keep  |
//|                                              http://www.mql4.com |
//+------------------------------------------------------------------+
#property copyright   "2005-2018, Xuejiayong."
#property link        "http://www.mql14.com"

//发送电子邮件，参数subject为邮件主题，some_text为邮件内容 void SendMail( string subject, string some_text)

//通用宏定义
//////////////////////////////////////////

//定义本程序所有的买卖点所对应的范围，原则上同一个外汇商处其他程序运行的时候不要在一个MAINMAGIC范围内
#define MAINMAGIC  10000000


/*定义多货币对数量*/
#define HFOREXNUMBER 60
/*定义货币对的时间周期数量*/
#define HTIMEPERIOD 6


//定义某一个外汇对的算法总数，不同周期的算法也涵盖在内了
#define HBUYSELLALGNUM 20

//定义一个算法买卖点最多买卖的数量
#define HSUBBUYSELLPOINTNUM 16




// 定义boolcross数组的长度
#define HCROSSNUMBER  16
#define HSLBUYSELLREORD 5000
/*定义均值bool长度，主要是为了保证止损和止盈长度相对稳定，避免外汇出现大幅变动的时候，大幅放宽bool长度值，使得某一个外汇出现巨额盈亏的情况*/
#define HBOOLLENGTHNUMBER 50




//外汇商专用宏定义
//定义外汇商的交易服务器
//////////////////////////////////////////

//交易零点帐号
#define HXMSERVER "XMUK-Real 15"

//传统帐号，多次订单拒绝交易
//#define HXMSERVER "XM.COM-Real 15"

#define HFXCMSERVER "FXCM-USDReal04"
#define HFXPROSERVER "FxPro.com-Real06"
#define HMARKETSSERVER "STAGlobalInvestments-HK"
#define HEXNESSSERVER "Exness-Real3"
#define HICMARKETSSERVER "ICMarkets-Live07"
#define HTHINKMARKETSSERVER "ThinkForexUK-Live"
#define HLMAXSERVER "LMAX-LiveUK"
#define HFXOPENSERVER "FXOpenUK-ECN Live Server"
#define HTICKMILLSERVER "TickmillUK-Live03"
#define HDRAWINEXSERVER "Darwinex-Live"


#define HEXNESSSERVERDEMO "Exness-Trial2"
#define HTHINKMARKETSSERVERDEMO "ThinkForexAU-Demo"
#define HICMARKETSSERVERDEMO "ICMarkets-Demo03"
#define HFXPOENSERVERDEMO "FXOpenUK-ECN Demo Server"
#define HTICKMILLSERVERDEMO "Tickmill-DemoUK"
#define HDRAWINEXSERVERDEMO "Darwinex-Demo"
#define HDUCASCOPYSERVERDEMO "Dukascopy-DEMO-1"



#define HOANDASERVER ""

//结束外汇商专用宏定义
//////////////////////////////////////////

#define HENABLESENDORDERHUNG 1
#define HDISABLEORDERHUNG -1

//定义成不用sendorder hung方式，代码检测
int sendorderhungstatus = HDISABLEORDERHUNG;

/////////////////////////////////////////



#define ALGORITHMFLAGDISABLE -1
#define ALGORITHMFLAGENABLE 1

//定义买卖单的状态，空仓、挂单、开仓
#define HPENDINGSTATEEMPTY -1
#define HPENDINGSTATEHUNGSTOP 0
#define HPENDINGSTATEOPEN 1
#define HPENDINGSTATEING 2
//这个状态表明是一次成功的交易状态
#define HPENDINGSTATECLOSED 3
#define HPENDINGSTATEHUNGLIMIT 4




//定义默认的止损倍数，通常是对应主周期的boollength，对应于不同的算法可能要重新设置
#define HDEFAULTSTOPLOSS 4
//定义默认的止盈倍数，通常是对应主周期的boollength，对应于不同的算法可能要重新设置
#define HDEFALTTAKEPROFIT 12

//定义bool指标的默认输入参数值，对应于不同的算法可能要重新设置
#define HDEFALTIBOOLLEN 2.5
#define HDEFALTIBOOLLENL 1.8
#define HDEFALTIBOOLB 60
#define HDEFALTMOVEAV 2




//全局变量定义
//////////////////////////////////////////
/*定义全局交易指标，确保每天只会交易一波，true为使能，false为禁止全局交易*/
bool globaltradeflag = true;
//定义服务器时间和本地时间（北京时间）差
int globaltimezonediff = 5; 
  
// 定义外汇商服务器名称
string g_forexserver;

// 定义外汇对
string MySymbol[HFOREXNUMBER+1];
int symbolNum = HFOREXNUMBER;

// 定义时间周期
int timeperiod[HTIMEPERIOD+1];
int TimePeriodNum = HTIMEPERIOD;



//定义开始自学习的测试数据时间
datetime startselflearntime= D'2002.01.19 12:30:27'; 

//定义结束自学习的测试数据时间
datetime endselflearntime= D'2014.11.19 12:30:27'; 

//当前时间
//datetime endselflearntime= 0; 

/*重大重要数据时间，每个周末落实第二周的情况*/
//重大重要数据期间，现有所有订单以一分钟周期重新设置止损，放大止盈，不做额外的买卖

datetime feinongtime1= D'1980.07.19 12:30:27';  // Year Month Day Hours Minutes Seconds
int feilongtimeoffset1 = 30*60;

datetime feinongtime2= D'1980.07.19 12:30:27';  // Year Month Day Hours Minutes Seconds
int feilongtimeoffset2 = 30*60;

datetime yixitime1 =   D'1980.07.19 12:30:27'; 
int yixitimeoffset1 = 2*60*60;

datetime yixitime2 =   D'1980.07.19 12:30:27'; 
int yixitimeoffset2 = 2*60*60;

datetime bigeventstime = D'1980.07.19 12:30:27'; 
int bigeventstimeoffset = 12*60*60;

//+------------------------------------------------------------------+
//|                                                                  |
//+------------------------------------------------------------------+

/////////////////////////////////////////////////////////////////////


//后面改为局部变量，尚未改动？？？？？
double ma_pre;
double boll_up_B_pre,boll_low_B_pre,boll_mid_B_pre;
//!!!!!!!!!!!!!!!!!!!!!!!!!


// 定义避免因错误导致的瞬间反复购买探测变量
int Freq_Count = 0;
int TwentyS_Freq = 0;
int OneM_Freq = 0;
int ThirtyS_Freq = 0;
int FiveM_Freq = 0;
int ThirtyM_Freq = 0;


//结束全局变量定义
//////////////////////////////////////////

//结构体定义
//////////////////////////////////////////

//定义某一个外汇的自有特征，包括隔夜利息等信息
struct stForexIndex
{
  //一个标准手使用资金
  double lotsize;
  //最小手数
  double minlot;
  //最大手数
  double maxlot;
  //改变标准手步幅
  double lotstep;
  //多头隔夜利息
  double swaplong;
  //空头隔夜利息
  double swapshort;

  //将其他货币或者商品兑换成美元的比值，用于计算一手交易的美元量
  double forexexchangevalue;

  double hlevel ;
  double htime;
};
stForexIndex ForexIndex[HFOREXNUMBER+1];

//定义某一个外汇在一个时间段上的特征，比如五分钟线的开盘或者收盘
struct stForexTimePeriod
{
  //tick结束时记录的是特定时间周期的barpos，如果对比发现该值跟ibar不同，说明此时是新的一个barpos的开始，也就是新条块的起点
  int ChartEvent;
};

stForexTimePeriod ForexTimePeriod[HFOREXNUMBER+1][HTIMEPERIOD+1];



//定义的是每一个外汇算法所需要的所有相关输入变量以及，该算法交易过程中所对应的中间变量，以及结果变量
struct stBuySellPosRecord
{ 


  //定义外汇是否有算法注册，只有在有算法注册的情况下才会执行后面的操作，在宏里面有对应的定义；
  int algorithmflag;

  //用来匹配算法注册初始化的函数，初始化函数和算法函数实现一一对应
  int algregistflag;
  //特定算法所使用的主时间周期，数组索引
  int timeperiodnum;
  //特定算法所使用的主时间周期，周期枚举值
  int my_timeperiod;

  int magicnumber;
  //Magicname的字符串描述
  string MagicName;

  //定义特定算法的实际同时存在的订单数量
  int subbuysellnumber;

  //特定算法对应的操作买卖点类型，设置为1为买类型，设置为-1为卖类型
  int buysellflag;

  //特定算法买卖点开单的时间，该买卖点可能会开多个单
  datetime opentime;

  //记录买卖的手数
  double orderlots;
  //定义开仓价格
  double openprice;
  //定义止损价格
  double stoploss;

  //定义基于时间换算调整后的止损价格，长时间后要合理的降低止损门槛
  double calstoploss;

  //定义止盈价格
  double takeprofit;

  //定义基于时间换算调整后的止盈价格，长时间后要合理的降低止盈预期
  double caltakeprofit;

  //开单对应的位置，仅用于挂单的时候计算超时时间
  int OneMOpenPos;

  //定义一个虚假的止损和止盈值，并设置到订单中去，实际的止损和止盈自动检测货币值，由代码发起；
  //防止恶意平台知道头寸后恶意止损，不给止盈；于此同时真出现黑天鹅事件时还能触发止损，该值定义为真正止损和止盈的2.5倍空间
  double fakedstoploss;
  double fakedtakeprofit;

  //测试挂单，实际上还是直接买卖
  int fakedtimeexp;

  //定义买卖状态，初步定义 -1空单状态 0为挂单状态 1为开单状态
  int pendingstate;

  //定义特定买卖点下的子交易是否存在
  int tradedflag;

  //定义循环止损值
  double stoptailing;

  //设置为2.1倍的stopless，将止损值降低为零 
  double stoptailtimes;

  //挂单开始计时设置，替换buystop和sellstop
  int timestart;
  //挂单超时设置  
  int timeexp;


  //录入订单经过一段时间以后再进入monitor程序；避免订单一直持有，寻找退出机制
  int keepperiod;

  //定义每个订单最大止损值，对应于当时账户值的百分比，作为设置止损值的依据
  double maxlose;

  //定义止损的尺度，根据不同的算法这个值可能会不一样的。
  double stoplossleverage;

  //定义止盈的尺度，根据不同的算法这个值可能会不一样的。
  double takeprofitleverage;

  //定义移动止盈止损函数是否生效
  int checkbuysellorderflag;

  //定义该外汇算法下的大bool的boollength
  double iBool_Len ;
  //定义该外汇算法下的小bool的boollength  
  double iBool_LenL ;
  //定义该算法下的bool长度
  int iBoll_B ;

  int Move_Av;

};

//第一维度是外汇，第二维度是第几买卖点，当前共有16个买卖点
//第三个维度是每个买卖点最多可以交易几次？原则上可以交易5次，每天最多交易一次，确保不会出现密集交易点。

stBuySellPosRecord BuySellPosRecord[HFOREXNUMBER+1][3*HBUYSELLALGNUM][HSUBBUYSELLPOINTNUM+1];

string SubMagicName[3*HBUYSELLALGNUM];

////////////////////////////////////////////////////////////////////////

// 定义每次均线交叉bool轨道期间对应的状态描述
//定义基于Bool轨道的过程数据值，用来给特定的算法作为判断参考依据，目前大部分的算法是围绕bool展开的，因为bool特征包含了概率统计的内容，并进行了归一化，具有比较高的实用价值；当然也不排除未来有些算法是基于其他特征过程构建
//所有的输入参数针对不同的算法是可调的
struct stBoolCrossRecord
{ 
  int CrossFlag[HCROSSNUMBER];//5 表示上穿上轨；4表示下穿上轨 1表示上穿中线 -1表示下穿中线 -5表示下穿下轨 -4表示上穿下轨
  double CrossStrongWeak[HCROSSNUMBER]; 
  double CrossTrend[HCROSSNUMBER];
  int CrossBoolPos[HCROSSNUMBER];
  
  int CrossFlagL[HCROSSNUMBER];//5 表示上穿上轨；4表示下穿上轨 1表示上穿中线 -1表示下穿中线 -5表示下穿下轨 -4表示上穿下轨
  double CrossStrongWeakL[HCROSSNUMBER];  
  double CrossTrendL[HCROSSNUMBER];
  int CrossBoolPosL[HCROSSNUMBER];
  double BoolFlagL; 
  int CrossFlagChangeL; 
        
  double BoolLength[HBOOLLENGTHNUMBER];
  int BoolLengthFlag;
  double AverageBoolLength;

  double StrongWeak;  //多头空头状态
  double Trend;//定义上涨下跌趋势
  double MoreTrend;//定义上涨下跌加速趋势
  double BoolIndex;
  double BoolFlag;  
  int CrossFlagChange;
  int CrossFlagTemp;  
  int CrossFlagTempPre; 

};
stBoolCrossRecord BoolCrossRecord[HFOREXNUMBER+1][3*HBUYSELLALGNUM][HTIMEPERIOD+1];


////////////////////////////////////////////
//结束结构体定义
//////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////


//设置全局变量，定义是否开启趋势买卖点、趋势转折买卖点、趋势转折挂单买卖点；通过修改全局变量可以在线改变
void initglobaltrig()
{

  double Trend_Keep;
  double Trend_Break;
  double Trend_Break_Hang;
  int i; 


  //设置全局变量g_Trend_Keep，正数开启趋势买卖点
  if(GlobalVariableCheck("g_Trend_Keep") == TRUE)
  {  
    GlobalVariableSet("g_Trend_Keep",1);
    Trend_Keep = GlobalVariableGet("g_Trend_Keep");    
    Print("g_Trend_Keep already exist  = "+DoubleToString(Trend_Keep));        
  }
  else
  {

    GlobalVariableSet("g_Trend_Keep",1);
    if(GlobalVariableCheck("g_Trend_Keep") == FALSE)
    {
      Print("init False due to g_Trend_Keep set false!");  
      return ;                  
    }       
    else
    {
      Trend_Keep = GlobalVariableGet("g_Trend_Keep");  
      Print("init g_Trend_Keep is OK  = "+DoubleToString(Trend_Keep));                                
    }  

  }

  //设置全局变量g_Trend_Break，正数开启趋势转折买卖点
  if(GlobalVariableCheck("g_Trend_Break") == TRUE)
  {  
    GlobalVariableSet("g_Trend_Break",1);
    Trend_Break = GlobalVariableGet("g_Trend_Break");    
    Print("g_Trend_Break already exist  = "+DoubleToString(Trend_Break));        
  }
  else
  {

    GlobalVariableSet("g_Trend_Break",1);
    if(GlobalVariableCheck("g_Trend_Break") == FALSE)
    {
      Print("init False due to g_Trend_Break set false!");  
      return ;                  
    }       
    else
    {
      Trend_Break = GlobalVariableGet("g_Trend_Break");  
      Print("init g_Trend_Break is OK  = "+DoubleToString(Trend_Break));                                
    }  

  }

  //设置全局变量g_Trend_Break_Hang，正数开启趋势转折挂单买卖点
  if(GlobalVariableCheck("g_Trend_Break_Hang") == TRUE)
  {  
    GlobalVariableSet("g_Trend_Break_Hang",1);
    Trend_Break_Hang = GlobalVariableGet("g_Trend_Break_Hang");    
    Print("g_Trend_Break_Hang already exist  = "+DoubleToString(Trend_Break_Hang));        
  }
  else
  {

    GlobalVariableSet("g_Trend_Break_Hang",1);
    if(GlobalVariableCheck("g_Trend_Break_Hang") == FALSE)
    {
      Print("init False due to g_Trend_Break_Hang set false!");  
      return ;                  
    }       
    else
    {
      Trend_Break_Hang = GlobalVariableGet("g_Trend_Break_Hang");  
      Print("init g_Trend_Break_Hang is OK  = "+DoubleToString(Trend_Break_Hang));                                
    }  

  }



      //等待所有周期的全局参数起来
      
  for (i = 0; i < 50; i++)
  {

    if((GlobalVariableCheck("g_Trend_Keep") == FALSE)
    ||(GlobalVariableCheck("g_Trend_Break") == FALSE)
    ||(GlobalVariableCheck("g_Trend_Break_Hang") == FALSE))
    {
      Print("waiting for globle_Value init,another ten seconds......" );      
      Sleep(10000);     
    }
    else
    {
      break;
    }

    
  }
  //无法启动所有全局变量
  if(i ==50)
  {
    Print("init global flag false due to open or set global_virable false"); 
    return ;     
  }
  else
  {
     Print( " init global flag successful !!! ");   
    //         SendNotification(MailTitlle + " init successful !!! "); 

  }

}

void deinitglobaltrig()
{


  if(GlobalVariableCheck("g_Trend_Keep") == TRUE)
  {      
    GlobalVariableDel("g_Trend_Keep");
  }  

  if(GlobalVariableCheck("g_Trend_Break") == TRUE)
  {      
    GlobalVariableDel("g_Trend_Break");
  }  

  if(GlobalVariableCheck("g_Trend_Break_Hang") == TRUE)
  {      
    GlobalVariableDel("g_Trend_Break_Hang");
  }  

}

// 连接到不同外汇商的实体服务器上，并针对不同的外汇商定义对应的外汇操作集合
//部分外汇服务商的参数配置不完整
 // 初始化外汇集合，打开该外汇服务器上所有可能参加外汇运算的外汇对，具体哪些外汇参加运算，根据后面注册的不同算法来确定
 //通过forexexchangevalue定义外汇跟账户货币的换算比值
void initsymbolall()
{
  string subject="";
  g_forexserver = AccountServer();

  subject = g_forexserver +"Init Email Send Test is Good!";
  SendMail( subject, "");
  //Print(subject);
    MySymbol[0] = "EURUSD";
    symbolNum = 1; 
    return;   
   
  if(AccountServer() == HXMSERVER)
  {   
    MySymbol[0] = "EURUSD.";
    MySymbol[1] = "AUDUSD.";
    MySymbol[2] = "USDJPY.";         
    MySymbol[3] = "GOLD.";         
    MySymbol[4] = "GBPUSD.";         
    MySymbol[5] = "CADCHF."; 
    MySymbol[6] = "EURCAD.";  
    MySymbol[7] = "GBPAUD.";  
    MySymbol[8] = "AUDJPY.";         
    MySymbol[9] = "EURJPY."; 
    MySymbol[10] = "GBPJPY.";   
    MySymbol[11] = "USDCAD."; 
    MySymbol[12] = "AUDCAD.";   
    MySymbol[13] = "AUDCHF."; 
    MySymbol[14] = "CADJPY."; 
    MySymbol[15] = "EURAUD."; 
    MySymbol[16] = "GBPCHF."; 
    MySymbol[17] = "NZDCAD."; 
    MySymbol[18] = "NZDUSD."; 
    MySymbol[19] = "NZDJPY."; 
    MySymbol[20] = "USDCHF.";   
    MySymbol[21] = "EURGBP.";   
    MySymbol[22] = "EURCHF.";   
    MySymbol[23] = "AUDNZD.";   
    MySymbol[24] = "CHFJPY.";   
    MySymbol[25] = "EURNZD.";     
    MySymbol[26] = "GBPCAD.";   
    MySymbol[27] = "GBPNZD.";     
    MySymbol[28] = "USDSGD.";   
    MySymbol[29] = "USDZAR.";   
  
    
    symbolNum = 30;

    openallsymbo();
    
  }
  else if(AccountServer() == HFXCMSERVER)
  {

    MySymbol[0] = "EURCAD";     
    MySymbol[1] = "AUDJPY";     
    MySymbol[2] = "EURNZD";   
    MySymbol[3] = "GBPUSD";     
    MySymbol[4] = "USDCHF";   
    MySymbol[5] = "AUDNZD"; 
    MySymbol[6] = "EURCHF";   
    MySymbol[7] = "EURUSD";
    MySymbol[8] = "NZDJPY"; 
    MySymbol[9] = "USDJPY";     
    MySymbol[10] = "AUDUSD";        
    MySymbol[11] = "EURGBP";  
    MySymbol[12] = "GBPCHF"; 
    MySymbol[13] = "NZDUSD";    
    MySymbol[14] = "EURAUD"; 
    MySymbol[15] = "EURJPY";        
    MySymbol[16] = "GBPJPY";  
    MySymbol[17] = "USDCAD"; 
    MySymbol[18] = "GBPAUD";    
    MySymbol[19] = "GBPNZD";    
    MySymbol[20] = "CADJPY";         
    MySymbol[21] = "XAUUSD";  
    
    /*           
    MySymbol[5] = "CADCHF"; 
    MySymbol[12] = "AUDCAD";  
    MySymbol[13] = "AUDCHF"; 
    MySymbol[17] = "NZDCAD";  
    MySymbol[24] = "CHFJPY";      
    MySymbol[26] = "GBPCAD";  
    MySymbol[28] = "USDSGD";  
    MySymbol[29] = "USDZAR";  
    */
    
    
    symbolNum = 22;
    openallsymbo();
  }   
  else if(AccountServer() == HFXPROSERVER)
  {
    MySymbol[0] = "AUDUSD";
    MySymbol[1] = "EURCHF";
    MySymbol[2] = "EURGBP";         
    MySymbol[3] = "EURJPY";         
    MySymbol[4] = "EURUSD";         
    MySymbol[5] = "GBPCHF"; 
    MySymbol[6] = "GBPJPY";   
    MySymbol[7] = "GBPUSD";   
    MySymbol[8] = "NZDUSD";         
    MySymbol[9] = "USDCAD"; 
    MySymbol[10] = "USDCHF";  
    MySymbol[11] = "USDJPY"; 
    MySymbol[12] = "AUDCAD";  
    MySymbol[13] = "AUDCHF"; 
    MySymbol[14] = "AUDJPY"; 
    MySymbol[15] = "AUDNZD"; 
    MySymbol[16] = "CADCHF"; 
    MySymbol[17] = "CADJPY"; 
    MySymbol[18] = "CHFJPY"; 
    MySymbol[19] = "EURAUD"; 
    MySymbol[20] = "EURCAD";  
    MySymbol[21] = "EURNZD";  
    MySymbol[22] = "GBPAUD";  
    MySymbol[23] = "GBPCAD";  
    MySymbol[24] = "GBPNZD";  
    MySymbol[25] = "NZDCAD";    
    MySymbol[26] = "NZDCHF";  
    MySymbol[27] = "GOLD";      
        
    symbolNum = 28;
    openallsymbo();
    
  } 
  else if(AccountServer() == HMARKETSSERVER)
  {
    MySymbol[0] = "AUDCAD";
    MySymbol[1] = "AUDCHF";
    MySymbol[2] = "AUDJPY";         
    MySymbol[3] = "AUDNZD";         
    MySymbol[4] = "AUDUSD";         
    MySymbol[5] = "CADCHF"; 
    MySymbol[6] = "CADJPY";   
    MySymbol[7] = "CHFJPY";   
    MySymbol[8] = "EURAUD";         
    MySymbol[9] = "EURCAD"; 
    MySymbol[10] = "EURCHF";  
    MySymbol[11] = "EURGBP"; 
    MySymbol[12] = "EURJPY";  
    MySymbol[13] = "EURNZD"; 
    MySymbol[14] = "EURUSD"; 
    MySymbol[15] = "GBPAUD"; 
    MySymbol[16] = "GBPCAD"; 
    MySymbol[17] = "GBPCHF"; 
    MySymbol[18] = "GBPJPY"; 
    MySymbol[19] = "GBPNZD"; 
    MySymbol[20] = "GBPUSD";  
    MySymbol[21] = "NZDCAD";  
    MySymbol[22] = "NZDCHF";  
    MySymbol[23] = "NZDJPY";  
    MySymbol[24] = "NZDUSD";  
    MySymbol[25] = "USDCAD";  
    MySymbol[26] = "USDCHF";      
    MySymbol[27] = "USDJPY";  
    MySymbol[28] = "XAUUSD";      
        
    symbolNum = 29;
    openallsymbo();
  } 
  else if(AccountServer() == HEXNESSSERVER)
  {
    MySymbol[0] = "AUDCADe";
    MySymbol[1] = "AUDCHFe";
    MySymbol[2] = "AUDJPYe";         
    MySymbol[3] = "AUDNZDe";         
    MySymbol[4] = "AUDUSDe";         
    MySymbol[5] = "CADCHFe"; 
    MySymbol[6] = "CADJPYe";  
    MySymbol[7] = "CHFJPYe";  
    MySymbol[8] = "EURAUDe";         
    MySymbol[9] = "EURCADe"; 
    MySymbol[10] = "EURCHFe";   
    MySymbol[11] = "EURGBPe"; 
    MySymbol[12] = "EURJPYe";   
    MySymbol[13] = "EURNZDe"; 
    MySymbol[14] = "EURUSDe"; 
    MySymbol[15] = "GBPAUDe"; 
    MySymbol[16] = "GBPCADe"; 
    MySymbol[17] = "GBPCHFe";   
    MySymbol[18] = "GBPJPYe"; 
    MySymbol[19] = "GBPNZDe"; 
    MySymbol[20] = "GBPUSDe";   
    MySymbol[21] = "NZDJPYe";   
    MySymbol[22] = "NZDUSDe";   
    MySymbol[23] = "USDCADe";   
    MySymbol[24] = "USDCHFe";   
    MySymbol[25] = "USDJPYe";   
    MySymbol[26] = "USDSGDe";     
          
    //MySymbol[26] = "XAUUSDe";  
          
    
    //MySymbol[28] = "NZDCADe"; 
        
    symbolNum = 27;
    openallsymbo();
  } 
  else if(AccountServer() == HEXNESSSERVERDEMO)
  {
    MySymbol[0] = "AUDCADm";
    MySymbol[1] = "AUDCHFm";
    MySymbol[2] = "AUDJPYm";         
    MySymbol[3] = "AUDNZDm";         
    MySymbol[4] = "AUDUSDm";         
    MySymbol[5] = "CADCHFm"; 
    MySymbol[6] = "CADJPYm";  
    MySymbol[7] = "CHFJPYm";  
    MySymbol[8] = "EURAUDk";         
    MySymbol[9] = "EURCADk"; 
    MySymbol[10] = "EURCHFk";   
    MySymbol[11] = "EURGBPf"; 
    MySymbol[12] = "EURJPYm";   
    MySymbol[13] = "EURNZDm"; 
    MySymbol[14] = "EURUSDk"; 
    MySymbol[15] = "GBPAUDk"; 
    MySymbol[16] = "GBPCADm"; 
    MySymbol[17] = "GBPCHFm";   
    MySymbol[18] = "GBPJPYm"; 
    MySymbol[19] = "GBPNZDm"; 
    MySymbol[20] = "GBPUSDm";   
    MySymbol[21] = "NZDJPYm";   
    MySymbol[22] = "NZDUSDm";   
    MySymbol[23] = "USDCADm";   
    MySymbol[24] = "USDCHFm";   
    MySymbol[25] = "USDJPYm";   
    MySymbol[26] = "USDSGDm";     
          
    MySymbol[27] = "XAUUSDm";  
          
    
    MySymbol[28] = "NZDCADm"; 
        
    symbolNum = 29;
    openallsymbo();
  }   
  else if(AccountServer() == HICMARKETSSERVER)
  {
    MySymbol[0] = "AUDCAD";
    MySymbol[1] = "AUDCHF";
    MySymbol[2] = "AUDJPY";         
    MySymbol[3] = "AUDNZD";         
    MySymbol[4] = "AUDUSD"; 

    MySymbol[5] = "CADCHF"; 
    MySymbol[6] = "CADJPY"; 

    MySymbol[7] = "CHFJPY"; 

    MySymbol[8] = "EURAUD";         
    MySymbol[9] = "EURCAD"; 
    MySymbol[10] = "EURCHF";  
    MySymbol[11] = "EURGBP"; 
    MySymbol[12] = "EURJPY";  
    MySymbol[13] = "EURNZD"; 
    MySymbol[14] = "EURUSD"; 

    MySymbol[15] = "GBPAUD"; 
    MySymbol[16] = "GBPCAD"; 
    MySymbol[17] = "GBPCHF"; 
    MySymbol[18] = "GBPJPY"; 
    MySymbol[19] = "GBPNZD"; 
    MySymbol[20] = "GBPUSD"; 

    MySymbol[21] = "NZDCAD";  
    MySymbol[22] = "NZDCHF";  
    MySymbol[23] = "NZDJPY";  
    MySymbol[24] = "NZDUSD"; 

    MySymbol[25] = "USDCAD";  
    MySymbol[26] = "USDCHF";      
    MySymbol[27] = "USDJPY";  
    MySymbol[28] = "XAUUSD";      
        
    symbolNum = 29;
    openallsymbo();


    ForexIndex[0].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);
    ForexIndex[1].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);
    ForexIndex[2].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);
    ForexIndex[3].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);   
    ForexIndex[4].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);

    if(MarketInfo(MySymbol[25],MODE_ASK) > 0.001)
    {
      ForexIndex[5].forexexchangevalue =  1/MarketInfo(MySymbol[25],MODE_ASK);
      ForexIndex[6].forexexchangevalue =  1/MarketInfo(MySymbol[25],MODE_ASK);

    }
    else
    {
      ForexIndex[5].forexexchangevalue =  1;
      ForexIndex[6].forexexchangevalue =  1;

      Print("Something was wrong with forex "+MySymbol[25] +"; value = "+ MarketInfo(MySymbol[25],MODE_ASK)); 
    }
  

    if(MarketInfo(MySymbol[26],MODE_ASK) > 0.001)
    {

      ForexIndex[7].forexexchangevalue =  1/MarketInfo(MySymbol[26],MODE_ASK);

    }
    else
    {
      ForexIndex[7].forexexchangevalue =  1;

      Print("Something was wrong with forex "+MySymbol[26] +"; value = "+ MarketInfo(MySymbol[26],MODE_ASK)); 
    }

    ForexIndex[8].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[9].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[10].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[11].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);   
    ForexIndex[12].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[13].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[14].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);

    ForexIndex[15].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK); 
    ForexIndex[16].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);
    ForexIndex[17].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);
    ForexIndex[18].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);
    ForexIndex[19].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);   
    ForexIndex[20].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);

    ForexIndex[21].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK);
    ForexIndex[22].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK);
    ForexIndex[23].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK); 
    ForexIndex[24].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK);

    ForexIndex[25].forexexchangevalue =  1;
    ForexIndex[26].forexexchangevalue =  1;
    ForexIndex[27].forexexchangevalue =  1; 
    ForexIndex[28].forexexchangevalue =  MarketInfo(MySymbol[28],MODE_ASK); 

  }   
    
  else if(AccountServer() == HTHINKMARKETSSERVER)
  {
    MySymbol[0] = "AUDCAD";
    MySymbol[1] = "AUDCHF";
    MySymbol[2] = "AUDJPY";         
    MySymbol[3] = "AUDNZD";         
    MySymbol[4] = "AUDUSD"; 

    MySymbol[5] = "CADCHF"; 
    MySymbol[6] = "CADJPY"; 

    MySymbol[7] = "CHFJPY";   

    MySymbol[8] = "EURAUD";         
    MySymbol[9] = "EURCAD"; 
    MySymbol[10] = "EURCHF";  
    MySymbol[11] = "EURGBP"; 
    MySymbol[12] = "EURJPY";  
    MySymbol[13] = "EURNZD"; 
    MySymbol[14] = "EURUSD"; 

    MySymbol[15] = "GBPAUD"; 
    MySymbol[16] = "GBPCAD"; 
    MySymbol[17] = "GBPCHF"; 
    MySymbol[18] = "GBPJPY"; 
    MySymbol[19] = "GBPNZD"; 
    MySymbol[20] = "GBPUSD"; 

    MySymbol[21] = "NZDCAD";  
    MySymbol[22] = "NZDCHF";  
    MySymbol[23] = "NZDJPY";  
    MySymbol[24] = "NZDUSD"; 

    MySymbol[25] = "USDCAD";  
    MySymbol[26] = "USDCHF";      
    MySymbol[27] = "USDJPY";  

    MySymbol[28] = "XAUUSDp";       
        
    symbolNum = 29;
    openallsymbo();

    ForexIndex[0].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);
    ForexIndex[1].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);
    ForexIndex[2].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);
    ForexIndex[3].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);   
    ForexIndex[4].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);

    if(MarketInfo(MySymbol[25],MODE_ASK) > 0.001)
    {
      ForexIndex[5].forexexchangevalue =  1/MarketInfo(MySymbol[25],MODE_ASK);
      ForexIndex[6].forexexchangevalue =  1/MarketInfo(MySymbol[25],MODE_ASK);
      ForexIndex[7].forexexchangevalue =  1/MarketInfo(MySymbol[26],MODE_ASK);

    }
    else
    {
      ForexIndex[5].forexexchangevalue =  1;
      ForexIndex[6].forexexchangevalue =  1;
      ForexIndex[7].forexexchangevalue =  1;

      Print("Something was wrong with forex "+MySymbol[25] +"; value = "+ MarketInfo(MySymbol[26],MODE_ASK)); 


    }
  

    ForexIndex[8].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[9].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[10].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[11].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);   
    ForexIndex[12].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[13].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[14].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);

    ForexIndex[15].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK); 
    ForexIndex[16].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);
    ForexIndex[17].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);
    ForexIndex[18].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);
    ForexIndex[19].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);   
    ForexIndex[20].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);

    ForexIndex[21].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK);
    ForexIndex[22].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK);
    ForexIndex[23].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK); 
    ForexIndex[24].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK);

    ForexIndex[25].forexexchangevalue =  1;
    ForexIndex[26].forexexchangevalue =  1;
    ForexIndex[27].forexexchangevalue =  1; 
    ForexIndex[28].forexexchangevalue =  MarketInfo(MySymbol[28],MODE_ASK); 

  } 

  else if(AccountServer() == HICMARKETSSERVERDEMO)
  {
    MySymbol[0] = "AUDCAD";
    MySymbol[1] = "AUDCHF";
    MySymbol[2] = "AUDJPY";         
    MySymbol[3] = "AUDNZD";         
    MySymbol[4] = "AUDUSD"; 

    MySymbol[5] = "CADCHF"; 
    MySymbol[6] = "CADJPY"; 

    MySymbol[7] = "CHFJPY";   

    MySymbol[8] = "EURAUD";         
    MySymbol[9] = "EURCAD"; 
    MySymbol[10] = "EURCHF";  
    MySymbol[11] = "EURGBP"; 
    MySymbol[12] = "EURJPY";  
    MySymbol[13] = "EURNZD"; 
    MySymbol[14] = "EURUSD"; 

    MySymbol[15] = "GBPAUD"; 
    MySymbol[16] = "GBPCAD"; 
    MySymbol[17] = "GBPCHF"; 
    MySymbol[18] = "GBPJPY"; 
    MySymbol[19] = "GBPNZD"; 
    MySymbol[20] = "GBPUSD"; 

    MySymbol[21] = "NZDCAD";  
    MySymbol[22] = "NZDCHF";  
    MySymbol[23] = "NZDJPY";  
    MySymbol[24] = "NZDUSD"; 

    MySymbol[25] = "USDCAD";  
    MySymbol[26] = "USDCHF";      
    MySymbol[27] = "USDJPY";  

    MySymbol[28] = "XAUUSD";  
    //symbolNum = 29;   
    /////////////////////////////////////////////   

    MySymbol[29] = "USDSGD";  
    MySymbol[30] = "XAGUSD"; 

    MySymbol[31] = "GBPSGD"; 
    MySymbol[32] = "GBPTRY";  
    MySymbol[33] = "NOKJPY";       
    MySymbol[34] = "NOKSEK";  
    MySymbol[35] = "SEKJPY";   
    MySymbol[36] = "SGDJPY";  

    MySymbol[37] = "USDCNH"; 
    MySymbol[38] = "USDCZK";  
    MySymbol[39] = "USDDKK";       
    MySymbol[40] = "USDHKD";  
    MySymbol[41] = "USDHUF";   
    MySymbol[42] = "USDMXN";      
    MySymbol[43] = "USDNOK"; 
    MySymbol[44] = "USDPLN";  
    MySymbol[45] = "USDRUB";       
    MySymbol[46] = "USDSEK";  
    MySymbol[47] = "USDTHB";  
    MySymbol[48] = "USDTRY";       
    MySymbol[49] = "USDZAR"; 

    MySymbol[50] = "AUDSGD";      
    MySymbol[51] = "CHFSGD"; 
    MySymbol[52] = "EURDKK";  
    MySymbol[53] = "EURHKD";       
    MySymbol[54] = "EURNOK";  
    MySymbol[55] = "EURPLN";   
    MySymbol[56] = "EURSEK";      
    MySymbol[57] = "EURSGD"; 
    MySymbol[58] = "EURTRY";  
    MySymbol[59] = "EURZAR";       
    MySymbol[60] = "GBPDKK";  
    MySymbol[61] = "GBPNOK";   
    MySymbol[62] = "GBPSEK";      

    MySymbol[63] = "XBRUSD";      
    MySymbol[64] = "US500"; 
    MySymbol[65] = "AUS200";  
    MySymbol[66] = "CHINA50";       
    MySymbol[67] = "DE30";  
    MySymbol[68] = "JP225";   
        
  MySymbol[0] = "EURUSD"; 
    symbolNum = 63;

    openallsymbo();




  } 

  else if(AccountServer() == HTHINKMARKETSSERVERDEMO)
  {


    MySymbol[0] = "AUDCAD";
    MySymbol[1] = "AUDCHF";
    MySymbol[2] = "AUDJPY";         
    MySymbol[3] = "AUDNZD";         
    MySymbol[4] = "AUDUSD"; 

    MySymbol[5] = "CADCHF"; 
    MySymbol[6] = "CADJPY"; 

    MySymbol[7] = "CHFJPY";   

    MySymbol[8] = "EURAUD";         
    MySymbol[9] = "EURCAD"; 
    MySymbol[10] = "EURCHF";  
    MySymbol[11] = "EURGBP"; 
    MySymbol[12] = "EURJPY";  
    MySymbol[13] = "EURNZD"; 
    MySymbol[14] = "EURUSD"; 

    MySymbol[15] = "GBPAUD"; 
    MySymbol[16] = "GBPCAD"; 
    MySymbol[17] = "GBPCHF"; 
    MySymbol[18] = "GBPJPY"; 
    MySymbol[19] = "GBPNZD"; 
    MySymbol[20] = "GBPUSD"; 

    MySymbol[21] = "NZDCAD";  
    MySymbol[22] = "NZDCHF";  
    MySymbol[23] = "NZDJPY";  
    MySymbol[24] = "NZDUSD"; 

    MySymbol[25] = "USDCAD";  
    MySymbol[26] = "USDCHF";      
    MySymbol[27] = "USDJPY";  

    MySymbol[28] = "XAUUSDp";       
        
    MySymbol[29] = "EURNOK";  
    MySymbol[30] = "EURSEK";      
    MySymbol[31] = "EURTRY";  
    MySymbol[32] = "USDCNH";        
    MySymbol[33] = "USDNOK";      
    MySymbol[34] = "USDSEK";  
    MySymbol[35] = "USDSGD";      
    MySymbol[36] = "USDTRY";  
    MySymbol[37] = "USDZAR";  

    MySymbol[38] = "SPX500"; 
    MySymbol[39] = "AUS200";  
    MySymbol[40] = "FRA40";       
    MySymbol[41] = "GER30"; 
    MySymbol[42] = "JPN225"; 
    MySymbol[43] = "UK100"; 


    symbolNum = 38;
    openallsymbo();


    ForexIndex[0].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);
    ForexIndex[1].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);
    ForexIndex[2].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);
    ForexIndex[3].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);   
    ForexIndex[4].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);

    if(MarketInfo(MySymbol[25],MODE_ASK) > 0.001)
    {
      ForexIndex[5].forexexchangevalue =  1/MarketInfo(MySymbol[25],MODE_ASK);
      ForexIndex[6].forexexchangevalue =  1/MarketInfo(MySymbol[25],MODE_ASK);
      ForexIndex[7].forexexchangevalue =  1/MarketInfo(MySymbol[26],MODE_ASK);

    }
    else
    {
      ForexIndex[5].forexexchangevalue =  1;
      ForexIndex[6].forexexchangevalue =  1;
      ForexIndex[7].forexexchangevalue =  1;

      Print("Something was wrong with forex "+MySymbol[25] +"; value = "+ MarketInfo(MySymbol[26],MODE_ASK)); 


    }
  

    ForexIndex[8].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[9].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[10].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[11].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);   
    ForexIndex[12].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[13].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[14].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);

    ForexIndex[15].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK); 
    ForexIndex[16].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);
    ForexIndex[17].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);
    ForexIndex[18].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);
    ForexIndex[19].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);   
    ForexIndex[20].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);

    ForexIndex[21].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK);
    ForexIndex[22].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK);
    ForexIndex[23].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK); 
    ForexIndex[24].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK);

    ForexIndex[25].forexexchangevalue =  1;
    ForexIndex[26].forexexchangevalue =  1;
    ForexIndex[27].forexexchangevalue =  1; 
    ForexIndex[28].forexexchangevalue =  MarketInfo(MySymbol[28],MODE_ASK); 


    ForexIndex[29].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[30].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[31].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[32].forexexchangevalue =  1;
    ForexIndex[33].forexexchangevalue =  1;
    ForexIndex[34].forexexchangevalue =  1; 
    ForexIndex[35].forexexchangevalue =  1;
    ForexIndex[36].forexexchangevalue =  1;
    ForexIndex[37].forexexchangevalue =  1; 
    ForexIndex[38].forexexchangevalue =  1; 
    ForexIndex[39].forexexchangevalue =  1;
    ForexIndex[40].forexexchangevalue =  1;
    ForexIndex[41].forexexchangevalue =  1; 
    ForexIndex[42].forexexchangevalue =  1;
    ForexIndex[43].forexexchangevalue =  1;


  }   
  else if(AccountServer() == HFXOPENSERVER)
  {
    MySymbol[0] = "AUDCAD";
    MySymbol[1] = "AUDCHF";
    MySymbol[2] = "AUDJPY";         
    MySymbol[3] = "AUDNZD";         
    MySymbol[4] = "AUDUSD";  

    MySymbol[5] = "CADCHF"; 
    MySymbol[6] = "CADJPY"; 

    MySymbol[7] = "CHFJPY";   

    MySymbol[8] = "EURAUD";         
    MySymbol[9] = "EURCAD"; 
    MySymbol[10] = "EURCHF";  
    MySymbol[11] = "EURGBP"; 
    MySymbol[12] = "EURJPY";  
    MySymbol[13] = "EURNZD"; 
    MySymbol[14] = "EURUSD"; 

    MySymbol[15] = "GBPAUD"; 
    MySymbol[16] = "GBPCAD"; 
    MySymbol[17] = "GBPCHF"; 
    MySymbol[18] = "GBPJPY"; 
    MySymbol[19] = "GBPNZD"; 
    MySymbol[20] = "GBPUSD"; 

    MySymbol[21] = "NZDCAD";  
    MySymbol[22] = "NZDCHF";  
    MySymbol[23] = "NZDJPY";  
    MySymbol[24] = "NZDUSD";  

    MySymbol[25] = "USDCAD";  
    MySymbol[26] = "USDCHF";      
    MySymbol[27] = "USDJPY";

    MySymbol[28] = "XAUUSD";      
        
    symbolNum = 29;
    openallsymbo();

    ForexIndex[0].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);
    ForexIndex[1].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);
    ForexIndex[2].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);
    ForexIndex[3].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);   
    ForexIndex[4].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);

    if(MarketInfo(MySymbol[25],MODE_ASK) > 0.001)
    {
      ForexIndex[5].forexexchangevalue =  1/MarketInfo(MySymbol[25],MODE_ASK);
      ForexIndex[6].forexexchangevalue =  1/MarketInfo(MySymbol[25],MODE_ASK);

    }
    else
    {
      ForexIndex[5].forexexchangevalue =  1;
      ForexIndex[6].forexexchangevalue =  1;

      Print("Something was wrong with forex "+MySymbol[25] +"; value = "+ MarketInfo(MySymbol[25],MODE_ASK)); 
    }
  

    if(MarketInfo(MySymbol[26],MODE_ASK) > 0.001)
    {

      ForexIndex[7].forexexchangevalue =  1/MarketInfo(MySymbol[26],MODE_ASK);

    }
    else
    {
      ForexIndex[7].forexexchangevalue =  1;

      Print("Something was wrong with forex "+MySymbol[26] +"; value = "+ MarketInfo(MySymbol[26],MODE_ASK)); 
    }
  

    ForexIndex[8].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[9].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[10].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[11].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);   
    ForexIndex[12].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[13].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[14].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);

    ForexIndex[15].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK); 
    ForexIndex[16].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);
    ForexIndex[17].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);
    ForexIndex[18].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);
    ForexIndex[19].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);   
    ForexIndex[20].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);

    ForexIndex[21].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK);
    ForexIndex[22].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK);
    ForexIndex[23].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK); 
    ForexIndex[24].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK);

    ForexIndex[25].forexexchangevalue =  1;
    ForexIndex[26].forexexchangevalue =  1;
    ForexIndex[27].forexexchangevalue =  1; 
    ForexIndex[28].forexexchangevalue =  MarketInfo(MySymbol[28],MODE_ASK);     

  } 
  else if(AccountServer() == HFXPOENSERVERDEMO)
  {
    MySymbol[0] = "AUDCAD";
    MySymbol[1] = "AUDCHF";
    MySymbol[2] = "AUDJPY";         
    MySymbol[3] = "AUDNZD";         
    MySymbol[4] = "AUDUSD";  

    MySymbol[5] = "CADCHF"; 
    MySymbol[6] = "CADJPY"; 

    MySymbol[7] = "CHFJPY";   

    MySymbol[8] = "EURAUD";         
    MySymbol[9] = "EURCAD"; 
    MySymbol[10] = "EURCHF";  
    MySymbol[11] = "EURGBP"; 
    MySymbol[12] = "EURJPY";  
    MySymbol[13] = "EURNZD"; 
    MySymbol[14] = "EURUSD"; 

    MySymbol[15] = "GBPAUD"; 
    MySymbol[16] = "GBPCAD"; 
    MySymbol[17] = "GBPCHF"; 
    MySymbol[18] = "GBPJPY"; 
    MySymbol[19] = "GBPNZD"; 
    MySymbol[20] = "GBPUSD"; 

    MySymbol[21] = "NZDCAD";  
    MySymbol[22] = "NZDCHF";  
    MySymbol[23] = "NZDJPY";  
    MySymbol[24] = "NZDUSD";  

    MySymbol[25] = "USDCAD";  
    MySymbol[26] = "USDCHF";      
    MySymbol[27] = "USDJPY";

    MySymbol[28] = "XAUUSD";      
        
    symbolNum = 29;
    openallsymbo();

    ForexIndex[0].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);
    ForexIndex[1].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);
    ForexIndex[2].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);
    ForexIndex[3].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);   
    ForexIndex[4].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);

    if(MarketInfo(MySymbol[25],MODE_ASK) > 0.001)
    {
      ForexIndex[5].forexexchangevalue =  1/MarketInfo(MySymbol[25],MODE_ASK);
      ForexIndex[6].forexexchangevalue =  1/MarketInfo(MySymbol[25],MODE_ASK);

    }
    else
    {
      ForexIndex[5].forexexchangevalue =  1;
      ForexIndex[6].forexexchangevalue =  1;

      Print("Something was wrong with forex "+MySymbol[25] +"; value = "+ MarketInfo(MySymbol[25],MODE_ASK)); 
    }
  

    if(MarketInfo(MySymbol[26],MODE_ASK) > 0.001)
    {

      ForexIndex[7].forexexchangevalue =  1/MarketInfo(MySymbol[26],MODE_ASK);

    }
    else
    {
      ForexIndex[7].forexexchangevalue =  1;

      Print("Something was wrong with forex "+MySymbol[26] +"; value = "+ MarketInfo(MySymbol[26],MODE_ASK)); 
    }

    ForexIndex[8].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[9].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[10].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[11].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);   
    ForexIndex[12].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[13].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[14].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);

    ForexIndex[15].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK); 
    ForexIndex[16].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);
    ForexIndex[17].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);
    ForexIndex[18].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);
    ForexIndex[19].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);   
    ForexIndex[20].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);

    ForexIndex[21].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK);
    ForexIndex[22].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK);
    ForexIndex[23].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK); 
    ForexIndex[24].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK);

    ForexIndex[25].forexexchangevalue =  1;
    ForexIndex[26].forexexchangevalue =  1;
    ForexIndex[27].forexexchangevalue =  1; 
    ForexIndex[28].forexexchangevalue =  MarketInfo(MySymbol[28],MODE_ASK);     

  } 

  else if(AccountServer() == HTICKMILLSERVER)
  {
    MySymbol[0] = "AUDCAD";
    MySymbol[1] = "AUDCHF";
    MySymbol[2] = "AUDJPY";         
    MySymbol[3] = "AUDNZD";         
    MySymbol[4] = "AUDUSD";   

    MySymbol[5] = "CADCHF"; 
    MySymbol[6] = "CADJPY"; 

    MySymbol[7] = "CHFJPY"; 

    MySymbol[8] = "EURAUD";         
    MySymbol[9] = "EURCAD"; 
    MySymbol[10] = "EURCHF";  
    MySymbol[11] = "EURGBP"; 
    MySymbol[12] = "EURJPY";  
    MySymbol[13] = "EURNZD"; 
    MySymbol[14] = "EURUSD"; 

    MySymbol[15] = "GBPAUD"; 
    MySymbol[16] = "GBPCAD"; 
    MySymbol[17] = "GBPCHF"; 
    MySymbol[18] = "GBPJPY"; 
    MySymbol[19] = "GBPNZD"; 
    MySymbol[20] = "GBPUSD";  

    MySymbol[21] = "NZDCAD";  
    MySymbol[22] = "NZDCHF";  
    MySymbol[23] = "NZDJPY";  
    MySymbol[24] = "NZDUSD"; 

    MySymbol[25] = "USDCAD";  
    MySymbol[26] = "USDCHF";      
    MySymbol[27] = "USDJPY";  
    MySymbol[28] = "XAUUSD";  



    symbolNum = 29;
    openallsymbo();

    ForexIndex[0].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);
    ForexIndex[1].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);
    ForexIndex[2].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);
    ForexIndex[3].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);   
    ForexIndex[4].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);

    if(MarketInfo(MySymbol[25],MODE_ASK) > 0.001)
    {
      ForexIndex[5].forexexchangevalue =  1/MarketInfo(MySymbol[25],MODE_ASK);
      ForexIndex[6].forexexchangevalue =  1/MarketInfo(MySymbol[25],MODE_ASK);

    }
    else
    {
      ForexIndex[5].forexexchangevalue =  1;
      ForexIndex[6].forexexchangevalue =  1;

      Print("Something was wrong with forex "+MySymbol[25] +"; value = "+ MarketInfo(MySymbol[25],MODE_ASK)); 
    }
  

    if(MarketInfo(MySymbol[26],MODE_ASK) > 0.001)
    {

      ForexIndex[7].forexexchangevalue =  1/MarketInfo(MySymbol[26],MODE_ASK);

    }
    else
    {
      ForexIndex[7].forexexchangevalue =  1;

      Print("Something was wrong with forex "+MySymbol[26] +"; value = "+ MarketInfo(MySymbol[26],MODE_ASK)); 
    }

    ForexIndex[8].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[9].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[10].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[11].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);   
    ForexIndex[12].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[13].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[14].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);

    ForexIndex[15].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK); 
    ForexIndex[16].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);
    ForexIndex[17].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);
    ForexIndex[18].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);
    ForexIndex[19].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);   
    ForexIndex[20].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);

    ForexIndex[21].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK);
    ForexIndex[22].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK);
    ForexIndex[23].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK); 
    ForexIndex[24].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK);

    ForexIndex[25].forexexchangevalue =  1;
    ForexIndex[26].forexexchangevalue =  1;
    ForexIndex[27].forexexchangevalue =  1; 
    ForexIndex[28].forexexchangevalue =  MarketInfo(MySymbol[28],MODE_ASK);       

  } 


  else if(AccountServer() == HDRAWINEXSERVER)
  {
    MySymbol[0] = "AUDCAD";
    MySymbol[1] = "AUDCHF";
    MySymbol[2] = "AUDJPY";         
    MySymbol[3] = "AUDNZD";         
    MySymbol[4] = "AUDUSD";   

    MySymbol[5] = "CADCHF"; 
    MySymbol[6] = "CADJPY"; 

    MySymbol[7] = "CHFJPY"; 

    MySymbol[8] = "EURAUD";         
    MySymbol[9] = "EURCAD"; 
    MySymbol[10] = "EURCHF";  
    MySymbol[11] = "EURGBP"; 
    MySymbol[12] = "EURJPY";  
    MySymbol[13] = "EURNZD"; 
    MySymbol[14] = "EURUSD"; 

    MySymbol[15] = "GBPAUD"; 
    MySymbol[16] = "GBPCAD"; 
    MySymbol[17] = "GBPCHF"; 
    MySymbol[18] = "GBPJPY"; 
    MySymbol[19] = "GBPNZD"; 
    MySymbol[20] = "GBPUSD";  

    MySymbol[21] = "NZDCAD";  
    MySymbol[22] = "NZDCHF";  
    MySymbol[23] = "NZDJPY";  
    MySymbol[24] = "NZDUSD"; 

    MySymbol[25] = "USDCAD";  
    MySymbol[26] = "USDCHF";      
    MySymbol[27] = "USDJPY";  
    MySymbol[28] = "XAUUSD";      

    symbolNum = 29;
    openallsymbo();

    ForexIndex[0].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);
    ForexIndex[1].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);
    ForexIndex[2].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);
    ForexIndex[3].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);   
    ForexIndex[4].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);

    if(MarketInfo(MySymbol[25],MODE_ASK) > 0.001)
    {
      ForexIndex[5].forexexchangevalue =  1/MarketInfo(MySymbol[25],MODE_ASK);
      ForexIndex[6].forexexchangevalue =  1/MarketInfo(MySymbol[25],MODE_ASK);

    }
    else
    {
      ForexIndex[5].forexexchangevalue =  1;
      ForexIndex[6].forexexchangevalue =  1;

      Print("Something was wrong with forex "+MySymbol[25] +"; value = "+ MarketInfo(MySymbol[25],MODE_ASK)); 
    }
  

    if(MarketInfo(MySymbol[26],MODE_ASK) > 0.001)
    {

      ForexIndex[7].forexexchangevalue =  1/MarketInfo(MySymbol[26],MODE_ASK);

    }
    else
    {
      ForexIndex[7].forexexchangevalue =  1;

      Print("Something was wrong with forex "+MySymbol[26] +"; value = "+ MarketInfo(MySymbol[26],MODE_ASK)); 
    }

    ForexIndex[8].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[9].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[10].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[11].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);   
    ForexIndex[12].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[13].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[14].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);

    ForexIndex[15].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK); 
    ForexIndex[16].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);
    ForexIndex[17].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);
    ForexIndex[18].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);
    ForexIndex[19].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);   
    ForexIndex[20].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);

    ForexIndex[21].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK);
    ForexIndex[22].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK);
    ForexIndex[23].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK); 
    ForexIndex[24].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK);

    ForexIndex[25].forexexchangevalue =  1;
    ForexIndex[26].forexexchangevalue =  1;
    ForexIndex[27].forexexchangevalue =  1; 
    ForexIndex[28].forexexchangevalue =  MarketInfo(MySymbol[28],MODE_ASK);       

  } 
  else if(AccountServer() == HDRAWINEXSERVERDEMO)
  {
    MySymbol[0] = "AUDCAD";
    MySymbol[1] = "AUDCHF";
    MySymbol[2] = "AUDJPY";         
    MySymbol[3] = "AUDNZD";         
    MySymbol[4] = "AUDUSD";   

    MySymbol[5] = "CADCHF"; 
    MySymbol[6] = "CADJPY"; 

    MySymbol[7] = "CHFJPY"; 

    MySymbol[8] = "EURAUD";         
    MySymbol[9] = "EURCAD"; 
    MySymbol[10] = "EURCHF";  
    MySymbol[11] = "EURGBP"; 
    MySymbol[12] = "EURJPY";  
    MySymbol[13] = "EURNZD"; 
    MySymbol[14] = "EURUSD"; 

    MySymbol[15] = "GBPAUD"; 
    MySymbol[16] = "GBPCAD"; 
    MySymbol[17] = "GBPCHF"; 
    MySymbol[18] = "GBPJPY"; 
    MySymbol[19] = "GBPNZD"; 
    MySymbol[20] = "GBPUSD";  

    MySymbol[21] = "NZDCAD";  
    MySymbol[22] = "NZDCHF";  
    MySymbol[23] = "NZDJPY";  
    MySymbol[24] = "NZDUSD"; 

    MySymbol[25] = "USDCAD";  
    MySymbol[26] = "USDCHF";      
    MySymbol[27] = "USDJPY";  
    MySymbol[28] = "XAUUSD";      

    symbolNum = 29;
    openallsymbo();

    ForexIndex[0].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);
    ForexIndex[1].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);
    ForexIndex[2].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);
    ForexIndex[3].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);   
    ForexIndex[4].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);

    if(MarketInfo(MySymbol[25],MODE_ASK) > 0.001)
    {
      ForexIndex[5].forexexchangevalue =  1/MarketInfo(MySymbol[25],MODE_ASK);
      ForexIndex[6].forexexchangevalue =  1/MarketInfo(MySymbol[25],MODE_ASK);

    }
    else
    {
      ForexIndex[5].forexexchangevalue =  1;
      ForexIndex[6].forexexchangevalue =  1;

      Print("Something was wrong with forex "+MySymbol[25] +"; value = "+ MarketInfo(MySymbol[25],MODE_ASK)); 
    }
  

    if(MarketInfo(MySymbol[26],MODE_ASK) > 0.001)
    {

      ForexIndex[7].forexexchangevalue =  1/MarketInfo(MySymbol[26],MODE_ASK);

    }
    else
    {
      ForexIndex[7].forexexchangevalue =  1;

      Print("Something was wrong with forex "+MySymbol[26] +"; value = "+ MarketInfo(MySymbol[26],MODE_ASK)); 
    }

    ForexIndex[8].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[9].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[10].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[11].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);   
    ForexIndex[12].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[13].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[14].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);

    ForexIndex[15].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK); 
    ForexIndex[16].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);
    ForexIndex[17].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);
    ForexIndex[18].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);
    ForexIndex[19].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);   
    ForexIndex[20].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);

    ForexIndex[21].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK);
    ForexIndex[22].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK);
    ForexIndex[23].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK); 
    ForexIndex[24].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK);

    ForexIndex[25].forexexchangevalue =  1;
    ForexIndex[26].forexexchangevalue =  1;
    ForexIndex[27].forexexchangevalue =  1; 
    ForexIndex[28].forexexchangevalue =  MarketInfo(MySymbol[28],MODE_ASK);       

  } 

  else if(AccountServer() == HDUCASCOPYSERVERDEMO)
  {
    MySymbol[0] = "AUDCAD";
    MySymbol[1] = "AUDCHF";
    MySymbol[2] = "AUDJPY";         
    MySymbol[3] = "AUDNZD";         
    MySymbol[4] = "AUDUSD";   

    MySymbol[5] = "CADCHF"; 
    MySymbol[6] = "CADJPY"; 

    MySymbol[7] = "CHFJPY"; 

    MySymbol[8] = "EURAUD";         
    MySymbol[9] = "EURCAD"; 
    MySymbol[10] = "EURCHF";  
    MySymbol[11] = "EURGBP"; 
    MySymbol[12] = "EURJPY";  
    MySymbol[13] = "EURNZD"; 
    MySymbol[14] = "EURUSD"; 

    MySymbol[15] = "GBPAUD"; 
    MySymbol[16] = "GBPCAD"; 
    MySymbol[17] = "GBPCHF"; 
    MySymbol[18] = "GBPJPY"; 
    MySymbol[19] = "GBPNZD"; 
    MySymbol[20] = "GBPUSD";  

    MySymbol[21] = "NZDCAD";  
    MySymbol[22] = "NZDCHF";  
    MySymbol[23] = "NZDJPY";  
    MySymbol[24] = "NZDUSD"; 

    MySymbol[25] = "USDCAD";  
    MySymbol[26] = "USDCHF";      
    MySymbol[27] = "USDJPY";  
    MySymbol[28] = "XAUUSD";    


    MySymbol[29] = "AUDSGD";  
    MySymbol[30] = "CHFSGD";  
    MySymbol[31] = "EURNOK";  
    MySymbol[32] = "EURPLN"; 
    MySymbol[33] = "EURSEK";  
    MySymbol[34] = "EURSGD";    
    MySymbol[35] = "SGDJPY";  
    MySymbol[36] = "USDNOK"; 
    MySymbol[37] = "USDPLN";  
    MySymbol[38] = "USDSEK";
    MySymbol[39] = "USDSGD";  
    MySymbol[40] = "USDZAR";     

    symbolNum = 41;
    openallsymbo();


    ForexIndex[0].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);
    ForexIndex[1].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);
    ForexIndex[2].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);
    ForexIndex[3].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);   
    ForexIndex[4].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);

    if(MarketInfo(MySymbol[25],MODE_ASK) > 0.001)
    {
      ForexIndex[5].forexexchangevalue =  1/MarketInfo(MySymbol[25],MODE_ASK);
      ForexIndex[6].forexexchangevalue =  1/MarketInfo(MySymbol[25],MODE_ASK);

    }
    else
    {
      ForexIndex[5].forexexchangevalue =  1;
      ForexIndex[6].forexexchangevalue =  1;

      Print("Something was wrong with forex "+MySymbol[25] +"; value = "+ MarketInfo(MySymbol[25],MODE_ASK)); 
    }
  

    if(MarketInfo(MySymbol[26],MODE_ASK) > 0.001)
    {

      ForexIndex[7].forexexchangevalue =  1/MarketInfo(MySymbol[26],MODE_ASK);

    }
    else
    {
      ForexIndex[7].forexexchangevalue =  1;

      Print("Something was wrong with forex "+MySymbol[26] +"; value = "+ MarketInfo(MySymbol[26],MODE_ASK)); 
    }

    ForexIndex[8].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[9].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[10].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[11].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);   
    ForexIndex[12].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[13].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[14].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);

    ForexIndex[15].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK); 
    ForexIndex[16].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);
    ForexIndex[17].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);
    ForexIndex[18].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);
    ForexIndex[19].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);   
    ForexIndex[20].forexexchangevalue =  MarketInfo(MySymbol[20],MODE_ASK);

    ForexIndex[21].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK);
    ForexIndex[22].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK);
    ForexIndex[23].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK); 
    ForexIndex[24].forexexchangevalue =  MarketInfo(MySymbol[24],MODE_ASK);

    ForexIndex[25].forexexchangevalue =  1;
    ForexIndex[26].forexexchangevalue =  1;
    ForexIndex[27].forexexchangevalue =  1; 
    ForexIndex[28].forexexchangevalue =  MarketInfo(MySymbol[28],MODE_ASK);   

    ForexIndex[29].forexexchangevalue =  MarketInfo(MySymbol[4],MODE_ASK);

    if(MarketInfo(MySymbol[26],MODE_ASK) > 0.001)
    {

      ForexIndex[30].forexexchangevalue =  1/MarketInfo(MySymbol[26],MODE_ASK);

    }
    else
    {
      ForexIndex[30].forexexchangevalue =  1;

      Print("Something was wrong with forex "+MySymbol[26] +"; value = "+ MarketInfo(MySymbol[26],MODE_ASK)); 
    }
    ForexIndex[31].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[32].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);
    ForexIndex[33].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);   
    ForexIndex[34].forexexchangevalue =  MarketInfo(MySymbol[14],MODE_ASK);

    if(MarketInfo(MySymbol[39],MODE_ASK) > 0.001)
    {

      ForexIndex[35].forexexchangevalue =  1/MarketInfo(MySymbol[39],MODE_ASK);

    }
    else
    {
      ForexIndex[35].forexexchangevalue =  1;

      Print("Something was wrong with forex "+MySymbol[39] +"; value = "+ MarketInfo(MySymbol[39],MODE_ASK)); 
    }

    ForexIndex[36].forexexchangevalue =  1;
    ForexIndex[37].forexexchangevalue =  1;
    ForexIndex[38].forexexchangevalue =  1;
    ForexIndex[39].forexexchangevalue =  1;
    ForexIndex[40].forexexchangevalue =  1;

  }     

  else if(AccountServer() == HLMAXSERVER)
  {
    MySymbol[0] = "AUDCAD.lmx";
    MySymbol[1] = "AUDCHF.lmx";
    MySymbol[2] = "AUDJPY.lmx";         
    MySymbol[3] = "AUDNZD.lmx";         
    MySymbol[4] = "AUDUSD.lmx";         
    MySymbol[5] = "CADCHF.lmx"; 
    MySymbol[6] = "CADJPY.lmx";   
    MySymbol[7] = "CHFJPY.lmx";   
    MySymbol[8] = "EURAUD.lmx";         
    MySymbol[9] = "EURCAD.lmx"; 
    MySymbol[10] = "EURCHF.lmx";  
    MySymbol[11] = "EURGBP.lmx"; 
    MySymbol[12] = "EURJPY.lmx";  
    MySymbol[13] = "EURNZD.lmx"; 
    MySymbol[14] = "EURUSD.lmx"; 
    MySymbol[15] = "GBPAUD.lmx"; 
    MySymbol[16] = "GBPCAD.lmx"; 
    MySymbol[17] = "GBPCHF.lmx"; 
    MySymbol[18] = "GBPJPY.lmx"; 
    MySymbol[19] = "GBPNZD.lmx"; 
    MySymbol[20] = "GBPUSD.lmx";  
    MySymbol[21] = "NZDCAD.lmx";  
    MySymbol[22] = "NZDCHF.lmx";  
    MySymbol[23] = "NZDJPY.lmx";  
    MySymbol[24] = "NZDUSD.lmx";  
    MySymbol[25] = "USDCAD.lmx";  
    MySymbol[26] = "USDCHF.lmx";      
    MySymbol[27] = "USDJPY.lmx";  
    MySymbol[28] = "XAUUSD.lmx";      
        
    symbolNum = 29;
    openallsymbo();
  }       
    
  else if(AccountServer() == HOANDASERVER)
  {
    MySymbol[0] = "EURUSD";
    MySymbol[1] = "AUDUSD";
    MySymbol[2] = "USDJPY";         
    MySymbol[3] = "XAUUSD-2";         
    MySymbol[4] = "GBPUSD";         
    MySymbol[5] = "CADCHF"; 
    MySymbol[6] = "EURCAD";   
    MySymbol[7] = "GBPAUD";   
    MySymbol[8] = "AUDJPY";         
    MySymbol[9] = "EURJPY"; 
    MySymbol[10] = "GBPJPY";  
    MySymbol[11] = "USDCAD"; 
    MySymbol[12] = "AUDCAD";  
    MySymbol[13] = "AUDCHF"; 
    MySymbol[14] = "CADJPY"; 
    MySymbol[15] = "EURAUD"; 
    MySymbol[16] = "GBPCHF"; 
    MySymbol[17] = "NZDCAD"; 
    MySymbol[18] = "NZDUSD"; 
    MySymbol[19] = "NZDJPY"; 
    MySymbol[20] = "USDCHF";
    
    MySymbol[21] = "EURGBP";  
    MySymbol[22] = "EURCHF";  
    MySymbol[23] = "AUDNZD";  
    MySymbol[24] = "CHFJPY";  
    MySymbol[25] = "EURNZD";  
    
    MySymbol[26] = "GBPCAD";  
    MySymbol[27] = "GBPNZD";  
    
    MySymbol[28] = "USDSGD";  
    MySymbol[29] = "USDZAR";  
  
      
    symbolNum = 4;
    openallsymbo();
  } 
  
  else
  {   


    symbolNum = 0;  
    Print("Bad Connect;Server name is ", AccountServer());  
        
  }
      
}

/*定义操作的时间周期集合*/
//用到周线，也是便于后期海量的测试。
void inittiimeperiod()
{
  timeperiod[0] = PERIOD_M1;
  timeperiod[1] = PERIOD_M5;
  timeperiod[2] = PERIOD_M30;
  timeperiod[3] = PERIOD_H4;
  timeperiod[4] = PERIOD_D1;
  timeperiod[5] = PERIOD_W1;
  
  TimePeriodNum = 6;
  
}

// 外汇商服务器连接测试，针对不同的服务器配置不同的初始参数，如时差
bool forexserverconnect()
{
  
  bool connectflag = false;
  int timezonecalcu;
  
  if(AccountServer() == HXMSERVER)
  {   
    
  
    //定义服务器时间和本地时间（北京时间）差
    globaltimezonediff = 5;     
    
    Print("Good Connect;Server name is ", AccountServer()); 
    connectflag = true;       
  }
  
  else if(AccountServer() == HFXCMSERVER)
  {
    
  
    //定义服务器时间和本地时间（北京时间）差
    globaltimezonediff = 5;     
        
    Print("Good Connect;Server name is ", AccountServer()); 
    connectflag = true;       
  }   
  else if(AccountServer() == HFXPROSERVER)
  {
    
  
    //定义服务器时间和本地时间（北京时间）差
    globaltimezonediff = 5; 
        
    Print("Good Connect;Server name is ", AccountServer()); 
    connectflag = true;       
  } 
  else if(AccountServer() == HMARKETSSERVER)
  {

  
    //定义服务器时间和本地时间（北京时间）差
    globaltimezonediff = 8; 
        
    Print("Good Connect;Server name is ", AccountServer()); 
    connectflag = true;       
  } 
  else if(AccountServer() == HEXNESSSERVER)
  {
  
    //定义服务器时间和本地时间（北京时间）差
    globaltimezonediff = 8; 
        
    Print("Good Connect;Server name is ", AccountServer()); 
    connectflag = true;       
  } 
  else if(AccountServer() == HEXNESSSERVERDEMO)
  {
  
    //定义服务器时间和本地时间（北京时间）差
    globaltimezonediff = 8; 
        
    Print("Good Connect;Server name is ", AccountServer()); 
    connectflag = true;       
  }   
    
  else if(AccountServer() == HICMARKETSSERVER)
  {
  
    //定义服务器时间和本地时间（北京时间）差
    globaltimezonediff = 5; 
        
    Print("Good Connect;Server name is ", AccountServer()); 
    connectflag = true;       
  }   
  else if(AccountServer() == HTHINKMARKETSSERVER)
  {
  
    //定义服务器时间和本地时间（北京时间）差
    globaltimezonediff = 6; 
        
    Print("Good Connect;Server name is ", AccountServer()); 
    connectflag = true;       
  } 
  else if(AccountServer() == HICMARKETSSERVERDEMO)
  {
  
    //定义服务器时间和本地时间（北京时间）差
    globaltimezonediff = 6; 
        
    Print("Good Connect;Server name is ", AccountServer()); 
    connectflag = true;       
  }

  else if(AccountServer() == HTHINKMARKETSSERVERDEMO)
  {
  
    //定义服务器时间和本地时间（北京时间）差
    globaltimezonediff = 6; 
        
    Print("Good Connect;Server name is ", AccountServer()); 
    connectflag = true;       
  }
    
  else if(AccountServer() == HFXOPENSERVER)
  {
  
    //定义服务器时间和本地时间（北京时间）差
    globaltimezonediff = 6; 
        
    Print("Good Connect;Server name is ", AccountServer()); 
    connectflag = true;       
  } 
  else if(AccountServer() == HFXPOENSERVERDEMO)
  {
  
    //定义服务器时间和本地时间（北京时间）差
    globaltimezonediff = 6; 
        
    Print("Good Connect;Server name is ", AccountServer()); 
    connectflag = true;       
  } 

  else if(AccountServer() == HTICKMILLSERVER)
  {
  
    //定义服务器时间和本地时间（北京时间）差
    globaltimezonediff = 6; 
        
    Print("Good Connect;Server name is ", AccountServer()); 
    connectflag = true;       
  }   

  else if(AccountServer() == HTICKMILLSERVERDEMO)
  {
  
    //定义服务器时间和本地时间（北京时间）差
    globaltimezonediff = 6; 
        
    Print("Good Connect;Server name is ", AccountServer()); 
    connectflag = true;       
  }
  else if(AccountServer() == HDRAWINEXSERVER)
  {
  
    //定义服务器时间和本地时间（北京时间）差
    globaltimezonediff = 6; 
        
    Print("Good Connect;Server name is ", AccountServer()); 
    connectflag = true;       
  } 
  else if(AccountServer() == HDRAWINEXSERVERDEMO)
  {
  
    //定义服务器时间和本地时间（北京时间）差
    globaltimezonediff = 6; 
        
    Print("Good Connect;Server name is ", AccountServer()); 
    connectflag = true;       
  }
  else if(AccountServer() == HDUCASCOPYSERVERDEMO)
  {
  
    //定义服务器时间和本地时间（北京时间）差
    globaltimezonediff = 6; 
        
    Print("Good Connect;Server name is ", AccountServer()); 
    connectflag = true;       
  }     

  else if(AccountServer() == HLMAXSERVER)
  {
  
    //定义服务器时间和本地时间（北京时间）差
    globaltimezonediff = 8; 
        
    Print("Good Connect;Server name is ", AccountServer()); 
    connectflag = true;       
  }       
  
  else if(AccountServer() == HOANDASERVER)
  {
    
  
    //定义服务器时间和本地时间（北京时间）差
    globaltimezonediff = 5; 
        
    Print("Good Connect;Server name is ", AccountServer()); 
    connectflag = true;       
  } 
  else
  {
    //定义服务器时间和本地时间（北京时间）差
    globaltimezonediff = 5;             
    Print("Bad Connect;Server name is ", AccountServer());  
    connectflag = false;        
  }
  
  timezonecalcu = int((TimeLocal()-TimeCurrent())/(60*60));
  if(timezonecalcu == globaltimezonediff)
  {
    Print("Set TimeZone success, globaltimezonediff = " + globaltimezonediff);
    connectflag = true;     
  }
  else
  {
    Print("Set TimeZone error, globaltimezonediff = " + globaltimezonediff+"But RealTimeZone timezonecalcu = "+timezonecalcu);
    globaltimezonediff = timezonecalcu;
    connectflag = true;     
  }
  return connectflag;

}


// 打开所有需要交易的外汇集合，打开后才能进行交易，ducascopy也是有同样要求
void openallsymbo()
{
   
  int SymPos;

  string my_symbol;
  for(SymPos = 0; SymPos < symbolNum;SymPos++)
  {
    
    my_symbol =   MySymbol[SymPos];
    
    if(SymbolSelect(my_symbol,true)==false)
    {
          Print("Open symbo error :" + my_symbol);
    }
   }

}


// 设置正常交易的全局交易开关，关闭的情况下不进行任何交易，一波交易完成后当天不再进行任何交易
void setglobaltradeflag(bool flag)
{

  globaltradeflag = flag;
}

// 获取正常交易的全局交易开关
bool getglobaltradeflag(void)
{

  return globaltradeflag ;
}


/*启动时初始化正常交易的全局交易标记*/
void initglobaltradeflag()
{

  datetime timelocal; 

  /*原则上采用服务器交易时间，为了便于人性化处理，做了一个转换*/ 
  timelocal = TimeCurrent() + globaltimezonediff*60*60;

  //14点前不做趋势单，主要针对1分钟线和五分钟线，非欧美时间趋势不明显，针对趋势突破单，要用这个来检测
  //最原始的是下午4点前不做趋势单，通过扩大止损来寻找更多机会

  if ((TimeHour(timelocal) >= 8 )&& (TimeHour(timelocal) <22 )) 
  {
    
    setglobaltradeflag(true);   
        
  } 
  else
  {
    setglobaltradeflag(false);        
  }

}

/*在交易时间段来临前确保使能全局交易标记*/
// 下午13点开始使能正常交易
void enableglobaltradeflag()
{
  int SymPos;
  int timeperiodnum;
  int my_timeperiod;
  string my_symbol;
    
  datetime timelocal; 

  SymPos = 0;
  /*每隔五分钟算一次*/
  timeperiodnum = 1;
  
  my_symbol =   MySymbol[SymPos]; 
  my_timeperiod = timeperiod[timeperiodnum];  
  

  /*原则上采用服务器交易时间，为了便于人性化处理，做了一个转换*/ 
  timelocal = TimeCurrent() + globaltimezonediff*60*60;


  /*确保交易时间段，来临前开启全局交易交易标记*/
  if ((TimeHour(timelocal) >= 8 )&& (TimeHour(timelocal) <9 )) 
  {     
    //确保是每个周期五分钟计算一次，而不是每个tick计算一次
    if ( ForexTimePeriod[SymPos][timeperiodnum].ChartEvent != iBars(my_symbol,my_timeperiod))
    {   
      //if(false == getglobaltradeflag())
      {
        setglobaltradeflag(true);   
        //Print("Enable Global Trade!");            
      }
      
    }
  } 
  
}

//初始化特定外汇对的特征，如隔夜利息等
void initforexindex()
{
  int SymPos;
  string my_symbol;
  for(SymPos = 0; SymPos < symbolNum;SymPos++)
  {
    
    my_symbol =   MySymbol[SymPos];
    ForexIndex[SymPos].lotsize = MarketInfo(my_symbol,MODE_LOTSIZE)*ForexIndex[SymPos].forexexchangevalue;
    ForexIndex[SymPos].minlot = MarketInfo(my_symbol,MODE_MINLOT);
    ForexIndex[SymPos].maxlot = MarketInfo(my_symbol,MODE_MAXLOT);
    ForexIndex[SymPos].lotstep = MarketInfo(my_symbol,MODE_LOTSTEP);
    ForexIndex[SymPos].swaplong = MarketInfo(my_symbol,MODE_SWAPLONG);
    ForexIndex[SymPos].swapshort = MarketInfo(my_symbol,MODE_SWAPSHORT);

   // Print(my_symbol+" ForexIndex["+SymPos+"][" +"lotsize = "+ForexIndex[SymPos].lotsize +";minlot = "+ForexIndex[SymPos].minlot
   //     +";maxlot = "+ForexIndex[SymPos].maxlot+";lotstep = "+ForexIndex[SymPos].lotstep+";swaplong = "+ForexIndex[SymPos].swaplong
   //     +";swapshort = "+ForexIndex[SymPos].swapshort); 

  }

}

//根据账户总额设置交易的风险偏好，原则上账户总额越大，承受的MaxLoses比例越小
//原则上账户每次提升500美金，风险降低0.0X%
//BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].maxlose 是跟算法和几分钟的买卖点相关的
void autoadjustmaxlose()
{
  
  int SymPos;
  int buysellpoint;
  int subbuysellpoint;

  int timeperiodnum;
  int my_timeperiod;
  string my_symbol;
    
  datetime timelocal; 

  SymPos = 0;
  /*每隔五分钟算一次*/
  timeperiodnum = 1;
  
  my_symbol =   MySymbol[SymPos]; 
  my_timeperiod = timeperiod[timeperiodnum];  
  

  /*原则上采用服务器交易时间，为了便于人性化处理，做了一个转换*/ 
  timelocal = TimeCurrent() + globaltimezonediff*60*60;


  //确保是每个周期五分钟计算一次，而不是每个tick计算一次,8-22点寻找交易点
  if ( ForexTimePeriod[SymPos][timeperiodnum].ChartEvent != iBars(my_symbol,my_timeperiod))
  {   
    initglobaltradeflag();
  }


  /*确保交易时间段，来临前开启全局交易交易标记*/
  if ((TimeHour(timelocal) >= 8 )&& (TimeHour(timelocal) <9 )) 
  { 
    
    //确保是每个周期五分钟计算一次，而不是每个tick计算一次
    if ( ForexTimePeriod[SymPos][timeperiodnum].ChartEvent != iBars(my_symbol,my_timeperiod))
    {         

      for(SymPos = 0; SymPos < symbolNum;SymPos++)
      {
        
        for(subbuysellpoint = 0; subbuysellpoint < HSUBBUYSELLPOINTNUM;subbuysellpoint++)
        {
          for(buysellpoint = 0; buysellpoint < HBUYSELLALGNUM*3;buysellpoint++)
          {
          
            //定义时间周期，一分钟的买卖点,趋势买卖点
            if ((buysellpoint < HBUYSELLALGNUM)&&(buysellpoint >= 0))
            {

              //每单允许损失的最大账户金额比例1%
              BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].maxlose = (0.02 - 0.001*(int((int(AccountBalance()))/500)))/2;                      

            }
            //定义时间周期，五分钟的买卖点
            else if((buysellpoint < HBUYSELLALGNUM*2)&&(buysellpoint >= HBUYSELLALGNUM))
            { 

              //每单允许损失的最大账户金额比例2%
              BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].maxlose = (0.04- 0.002*(int((int(AccountBalance()))/500)))/2;   

            }
            //定义时间周期，三十分钟的买卖点
            else if((buysellpoint < HBUYSELLALGNUM*3)&&(buysellpoint >= HBUYSELLALGNUM*2))
            { 

              //每单允许损失的最大账户金额比例2%
              BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].maxlose = (0.04- 0.002*(int((int(AccountBalance()))/500)))/2;   

            } 


            else
            {
              ;
            }

          }

        } 
                            
      }

  
    }   
    
  } 

}


/*初始化交易手数*/
// 根据不同的账户金额值来定义不同的单独交易允许的止损百分比
//自动计算交易手数，目前就是按照最低交易手数计算的
double autocalculateamount(int SymPos,int buysellpoint,int subbuysellpoint)
{


  double lastamount;
  
  double accountbalance = 0;


  //最小值作为基本单位；
  lastamount = ForexIndex[SymPos].minlot;


  return lastamount;

}


/*每天交易前计算交易手数，只在下午一点计算，每隔5分钟算一次*/
// 根据不同的账户金额值来定义不同的交易手数
//目前没有做计算
void autoadjustglobalamount()
{
  
  int SymPos;
  int timeperiodnum;
  int my_timeperiod;
  string my_symbol;
    
  datetime timelocal; 

  SymPos = 0;
  /*每隔五分钟算一次*/
  timeperiodnum = 1;
  
  my_symbol =   MySymbol[SymPos]; 
  my_timeperiod = timeperiod[timeperiodnum];  
  

  /*原则上采用服务器交易时间，为了便于人性化处理，做了一个转换*/ 
  timelocal = TimeCurrent() + globaltimezonediff*60*60;


  //确保是每个周期五分钟计算一次，而不是每个tick计算一次,8-22点寻找交易点
  if ( ForexTimePeriod[SymPos][timeperiodnum].ChartEvent != iBars(my_symbol,my_timeperiod))
  {   
    initglobaltradeflag();
  }


  /*确保交易时间段，来临前开启全局交易交易标记*/
  if ((TimeHour(timelocal) >= 8 )&& (TimeHour(timelocal) <9 )) 
  { 
    
    //确保是每个周期五分钟计算一次，而不是每个tick计算一次
    if ( ForexTimePeriod[SymPos][timeperiodnum].ChartEvent != iBars(my_symbol,my_timeperiod))
    {         

      //根据不同的账户值定义允许的单交易最大止损百分比
      if(AccountBalance() <= 2000)
      {
  
        //Print("autoadjustglobalamount Amount is = "+MyLotsH+":"+MyLotsL);         
      } 

      else
      {
        //Print("default autoadjustglobalamount Amount is = "+MyLotsH+":"+MyLotsL);               
      }   
      




      //每日刷新一次
      for(SymPos = 0; SymPos < symbolNum;SymPos++)
      {
        
        my_symbol =   MySymbol[SymPos];
        ForexIndex[SymPos].lotsize = MarketInfo(my_symbol,MODE_LOTSIZE);
        ForexIndex[SymPos].minlot = MarketInfo(my_symbol,MODE_MINLOT);
        ForexIndex[SymPos].maxlot = MarketInfo(my_symbol,MODE_MAXLOT);
        ForexIndex[SymPos].lotstep = MarketInfo(my_symbol,MODE_LOTSTEP);
        ForexIndex[SymPos].swaplong = MarketInfo(my_symbol,MODE_SWAPLONG);
        ForexIndex[SymPos].swapshort = MarketInfo(my_symbol,MODE_SWAPSHORT);

        //Print(my_symbol+" ForexIndex["+SymPos+"][" +"lotsize = "+ForexIndex[SymPos].lotsize +";minlot = "+ForexIndex[SymPos].minlot
        //    +";maxlot = "+ForexIndex[SymPos].maxlot+";lotstep = "+ForexIndex[SymPos].lotstep+";swaplong = "+ForexIndex[SymPos].swaplong
        //    +";swapshort = "+ForexIndex[SymPos].swapshort); 

      }




  
    }   
    
  }
  
}

// 判断MagicNumber是否已经存在交易，没有交易时返回true，挂单也是定义为open
// 确保每个买卖点只有一个交易存在,包含挂掉也放在里面，确保不会出现重复挂单
bool OneMOrderCloseStatus(int MagicNumber)
{
  bool status;
  int i;
  status = true;

  if ( OrdersTotal() > 200)
  {
    Print("OneMOrderKeepNumber exceed 200");
    return false;
  }
  
  for (i = 0; i < OrdersTotal(); i++)
  {
       if (OrderSelect(i,SELECT_BY_POS,MODE_TRADES))
       {
        //未平仓的订单和挂单交易的平仓时间等于0
      if((OrderCloseTime() == 0)&&(OrderMagicNumber()== MagicNumber))
      {

        status= false;
        break;

      }
                
       }
  }
   return status;
}


// 判断MagicNumber是否已经存在交易，没有交易时返回true，挂单定义为close
// 确保每个买卖点只有一个交易存在,包含挂掉也放在里面，确保不会出现重复挂单
bool OneMOrderRealCloseStatus(int MagicNumber)
{
  bool status;
  int i;
  status = true;

  if ( OrdersTotal() > 200)
  {
    Print("OneMOrderKeepNumber exceed 200");
    return false;
  }
  
  for (i = 0; i < OrdersTotal(); i++)
  {
       if (OrderSelect(i,SELECT_BY_POS,MODE_TRADES))
       {

      if((OrderType()==OP_BUY)||(OrderType()==OP_SELL))
      {       
          //未平仓的订单和挂单交易的平仓时间等于0
        if((OrderCloseTime() == 0)&&(OrderMagicNumber()== MagicNumber))
        {

          status= false;
          break;

        }
      }
                
       }
  }
   return status;
}




// 初始化定义买卖点的状态参数，主要是通用参数和默认参数
void InitBuySellPos()
{
  int SymPos;
  int i ;
  string my_symbol;
  double vbid;
  int buysellpoint;
  int subbuysellpoint;

  for(i = 0; i < 3*HBUYSELLALGNUM; i++)
  {
    //趋势买卖点
    if(i < 10)
    {
      SubMagicName[i] = "TradeNumber"+IntegerToString(0)+IntegerToString(i);
    }
    else if((i >= 10) &&(i < HBUYSELLALGNUM*3))
    {
      SubMagicName[i] = "TrradeNumber"+IntegerToString(i);     
    }
   
    else
    {
      ;
    }


  }



  for(SymPos = 0; SymPos < symbolNum;SymPos++)
  {
    
    my_symbol =   MySymbol[SymPos];
    vbid    = MarketInfo(my_symbol,MODE_BID); 
    for(subbuysellpoint = 0; subbuysellpoint < HSUBBUYSELLPOINTNUM;subbuysellpoint++)
    {
      for(buysellpoint = 0; buysellpoint < 3*HBUYSELLALGNUM;buysellpoint++)
      {

        //默认情况下所有的算法都是无效的，需要针对每个外汇的每个算法要单独注册，这样的话就可以确保不断的有新算法增加上去了；算法越多买卖的就越丰富，最终形成算法池。
        BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].algorithmflag = ALGORITHMFLAGDISABLE;

        //默认情况下算法注册初始化为空
        BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].algregistflag = -1;

        //定义买卖点名称
        BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].MagicName =SubMagicName[buysellpoint]+IntegerToString(subbuysellpoint)+my_symbol;

        //定义 MagicNumber
        BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].magicnumber = (SymPos+1)*MAINMAGIC + buysellpoint*100 + subbuysellpoint;

        //默认状态下设置所有的买卖状态为空仓状态
        BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].pendingstate = HPENDINGSTATEEMPTY;        


        

        //记录当前一分钟的ibar位置
        BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].OneMOpenPos =   iBars(MySymbol[SymPos],timeperiod[0]);


        //设置特定算法的同时交易订单数量，默认为最大值，特定算法中可修改
        BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].subbuysellnumber = HSUBBUYSELLPOINTNUM;   

        //设置止损和止盈的比例，这个是默认值，不同的算法需要重新进行设置，达到最好的效果
        BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoplossleverage = HDEFAULTSTOPLOSS;
        BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].takeprofitleverage = HDEFALTTAKEPROFIT; 
        //定义stoptailing为2.1
        BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoptailtimes = 2;                 
        BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].checkbuysellorderflag = ALGORITHMFLAGDISABLE;  


        BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].iBool_Len = HDEFALTIBOOLLEN;
        BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].iBool_LenL = HDEFALTIBOOLLENL;
        BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].iBoll_B = HDEFALTIBOOLB;          
        BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].Move_Av = HDEFALTMOVEAV;  




        //定义时间周期，一分钟的买卖点，趋势买卖点
        if ((buysellpoint < HBUYSELLALGNUM)&&(buysellpoint >= 0))
        {

          BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].timeperiodnum = 0;     
          BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].my_timeperiod = PERIOD_M1;          

          //挂单开始时间设置5分钟，或者5根1分钟线
          BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].timestart = 15; 
          //挂单超时时间设置4个小时，或者60根1分钟线
          BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].timeexp = 60*4; 

          //定义伪装挂单超时时间，单位是分钟，或者是1分钟线；统一为4分钟，确保不好成交，但是又在交易系统中有痕迹
          BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].fakedtimeexp = 60;          
          
          //持用4个小时以后进入monitor
          BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].keepperiod = 60*60*4; 

          //每单允许损失的最大账户金额比例2%
          BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].maxlose = (0.02 - 0.001*(int((int(AccountBalance()))/5000)))/4;                     

        }
        //定义时间周期，五分钟及以上的买卖点
        else if((buysellpoint < HBUYSELLALGNUM*2)&&(buysellpoint >= HBUYSELLALGNUM))
        {

          BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].timeperiodnum = 1;     
          BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].my_timeperiod = PERIOD_M5;     

          //挂单开始时间设置20分钟，或者20根1分钟线
          BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].timestart = 15; 
          //挂单超时时间设置12个小时
          BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].timeexp = 60*12;  

          //定义伪装挂单超时时间，单位是分钟，或者是1分钟线；统一为4分钟，确保不好成交，但是又在交易系统中有痕迹
          BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].fakedtimeexp = 60*4;  

          //持用12个小时以后进入monitor
          BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].keepperiod = 60*60*12;    

          //每单允许损失的最大账户金额比例5%
          BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].maxlose = (0.04- 0.002*(int((int(AccountBalance()))/5000)))/4;    

        }
        //定义时间周期，三十分钟及以上的买卖点
        else if((buysellpoint < HBUYSELLALGNUM*3)&&(buysellpoint >= HBUYSELLALGNUM*2))
        {

          BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].timeperiodnum = 2;     
          BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].my_timeperiod = PERIOD_M30;             

          //挂单开始时间设置20分钟，或者20根1分钟线
          BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].timestart = 15; 
          //挂单超时时间设置12个小时
          BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].timeexp = 60*12*8;  

          //定义伪装挂单超时时间，单位是分钟，或者是1分钟线；统一为4分钟，确保不好成交，但是又在交易系统中有痕迹
          BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].fakedtimeexp = 60*4*5;  

          //持用48个小时以后进入monitor
          BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].keepperiod = 60*60*48;    

          //每单允许损失的最大账户金额比例5%
          BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].maxlose = (0.04- 0.002*(int((int(AccountBalance()))/5000)))/4;    

        }   
             
        else
        {
          ;
        }


        //奇数定义为买点，偶数定义为卖点
        if((buysellpoint%2) ==1)
        {
          BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].buysellflag = 1;

        }
        else
        {
          BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].buysellflag = -1;

        }



      }

    } 
                        
  }

}



// 初始化定义boolcross的值
int  InitcrossValue(int SymPos, int buysellpoint,int timeperiodnum)
{ 
  double myma,myboll_up_B,myboll_low_B,myboll_mid_B,myboollength;
  double myma_pre,myboll_up_B_pre,myboll_low_B_pre,myboll_mid_B_pre;

  double StrongWeak;
  double MAFive,MAThentyOne,MASixty; 
  string my_symbol;
  int my_timeperiod;
  
  int crossflag;
  int j ;
  int i;
  int countnumber = 0;


  my_symbol =   MySymbol[SymPos];
  my_timeperiod = timeperiod[timeperiodnum];  
  
  //未注册的算法就不去计算ibool相关值了，因为用不上
  if(BuySellPosRecord[SymPos][buysellpoint][0].algorithmflag == ALGORITHMFLAGDISABLE)
  {
    return 0;
  }

  /*确保覆盖最近6年以内数据*/
  if(timeperiodnum<5)
  {
    countnumber = 2000000;
  }
  else if(timeperiodnum==5)
  {
    countnumber = 1000000;
  }
  else
  {
    countnumber = 800000;
  }
    
  if(iBars(my_symbol,my_timeperiod) <countnumber)
  {
    //Print(my_symbol + ":"+my_timeperiod+":Bar Number less than "+countnumber+"which is :" + iBars(my_symbol,my_timeperiod));
    countnumber = iBars(my_symbol,my_timeperiod) - 100;
    //return -1;
  }


  /*初始化boollength*/
  BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].BoolLengthFlag = 0;
  for (i = 0; i< HBOOLLENGTHNUMBER;i++)
  {
    BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].BoolLength[i] = 0;
  }
  BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].AverageBoolLength = 0.000001;

  j = 0;
  for (i = 2; i< countnumber;i++)
  {
    
    crossflag = 0;     
    myma=iMA(my_symbol,my_timeperiod,BuySellPosRecord[SymPos][buysellpoint][0].Move_Av,0,MODE_SMA,PRICE_CLOSE,i-1);  
    myboll_up_B = iBands(my_symbol,my_timeperiod,BuySellPosRecord[SymPos][buysellpoint][0].iBoll_B,BuySellPosRecord[SymPos][buysellpoint][0].iBool_Len,0,PRICE_CLOSE,MODE_UPPER,i-1);   
    myboll_low_B = iBands(my_symbol,my_timeperiod,BuySellPosRecord[SymPos][buysellpoint][0].iBoll_B,BuySellPosRecord[SymPos][buysellpoint][0].iBool_Len,0,PRICE_CLOSE,MODE_LOWER,i-1);
    myboll_mid_B = (  myboll_up_B +  myboll_low_B)/2;

    myma_pre = iMA(my_symbol,my_timeperiod,BuySellPosRecord[SymPos][buysellpoint][0].Move_Av,0,MODE_SMA,PRICE_CLOSE,i); 
    myboll_up_B_pre = iBands(my_symbol,my_timeperiod,BuySellPosRecord[SymPos][buysellpoint][0].iBoll_B,BuySellPosRecord[SymPos][buysellpoint][0].iBool_Len,0,PRICE_CLOSE,MODE_UPPER,i);      
    myboll_low_B_pre = iBands(my_symbol,my_timeperiod,BuySellPosRecord[SymPos][buysellpoint][0].iBoll_B,BuySellPosRecord[SymPos][buysellpoint][0].iBool_Len,0,PRICE_CLOSE,MODE_LOWER,i);
    myboll_mid_B_pre = (myboll_up_B_pre + myboll_low_B_pre)/2;

    if((myma >myboll_up_B) && (myma_pre < myboll_up_B_pre ) )
    {
        crossflag = 5;    
    }
    
    if((myma <myboll_up_B) && (myma_pre > myboll_up_B_pre ) )
    {
        crossflag = 4;
    }
      
    if((myma < myboll_low_B) && (myma_pre > myboll_low_B_pre ) )
    {
        crossflag = -5;
    }
      
    if((myma > myboll_low_B) && (myma_pre < myboll_low_B_pre ) )
    {
        crossflag = -4; 
    }
  
    if((myma > myboll_mid_B) && (myma_pre < myboll_mid_B_pre ))
    {
        crossflag = 1;        
    } 
    if( (myma < myboll_mid_B) && (myma_pre > myboll_mid_B_pre ))
    {
        crossflag = -1;               
    }     
    
    if(0 !=   crossflag)    
    {

      MAFive=iMA(my_symbol,my_timeperiod,5,0,MODE_SMA,PRICE_CLOSE,i); 
      MAThentyOne=iMA(my_symbol,my_timeperiod,21,0,MODE_SMA,PRICE_CLOSE,i); 
      MASixty=iMA(my_symbol,my_timeperiod,60,0,MODE_SMA,PRICE_CLOSE,i); 

      //定义多空状态指标
      StrongWeak =0.5;

      if(MAFive > MAThentyOne)
      {     
        /*多均线多头向上*/
        if(MASixty < MAThentyOne)
        {
           StrongWeak =0.9;
        }
        else if ((MASixty >= MAThentyOne) &&(MASixty <MAFive))
        {
           StrongWeak =0.6;
        }
        else
        {
           StrongWeak =0.5;
        }
      
      }
      else if (MAFive < MAThentyOne)
      {
        /*多均线多头向下*/
        if(MASixty > MAThentyOne)
        {
           StrongWeak =0.1;
        }
        else if ((MASixty <= MAThentyOne) &&(MASixty > MAFive))
        {
           StrongWeak =0.4;
        }
        else
        {
           StrongWeak =0.5;
        }   
      
      }
      else
      {
        StrongWeak =0.5;

      }

      BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossStrongWeak[j] = StrongWeak;
      BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossFlag[j] = crossflag;
      //BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossDatetime[j] = TimeCurrent() - i*Period()*60;
      BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossBoolPos[j] = iBars(my_symbol,my_timeperiod)-i;
      j++;
      if (j >= (HCROSSNUMBER-1))
      {
        break;
      }
    }

  }
  

  j = 0;
  for (i = 2; i< countnumber;i++)
  {
    
    crossflag = 0;     
    myma=iMA(my_symbol,my_timeperiod,BuySellPosRecord[SymPos][buysellpoint][0].Move_Av,0,MODE_SMA,PRICE_CLOSE,i-1);  
    myboll_up_B = iBands(my_symbol,my_timeperiod,BuySellPosRecord[SymPos][buysellpoint][0].iBoll_B,BuySellPosRecord[SymPos][buysellpoint][0].iBool_LenL,0,PRICE_CLOSE,MODE_UPPER,i-1);   
    myboll_low_B = iBands(my_symbol,my_timeperiod,BuySellPosRecord[SymPos][buysellpoint][0].iBoll_B,BuySellPosRecord[SymPos][buysellpoint][0].iBool_LenL,0,PRICE_CLOSE,MODE_LOWER,i-1);
    myboll_mid_B = (  myboll_up_B +  myboll_low_B)/2;
    myboollength = (  myboll_up_B -  myboll_low_B)/2;

    myma_pre = iMA(my_symbol,my_timeperiod,BuySellPosRecord[SymPos][buysellpoint][0].Move_Av,0,MODE_SMA,PRICE_CLOSE,i); 
    myboll_up_B_pre = iBands(my_symbol,my_timeperiod,BuySellPosRecord[SymPos][buysellpoint][0].iBoll_B,BuySellPosRecord[SymPos][buysellpoint][0].iBool_LenL,0,PRICE_CLOSE,MODE_UPPER,i);      
    myboll_low_B_pre = iBands(my_symbol,my_timeperiod,BuySellPosRecord[SymPos][buysellpoint][0].iBoll_B,BuySellPosRecord[SymPos][buysellpoint][0].iBool_LenL,0,PRICE_CLOSE,MODE_LOWER,i);
    myboll_mid_B_pre = (myboll_up_B_pre + myboll_low_B_pre)/2;

    if((myma >myboll_up_B) && (myma_pre < myboll_up_B_pre ) )
    {
        crossflag = 5;    
    }
    
    if((myma <myboll_up_B) && (myma_pre > myboll_up_B_pre ) )
    {
        crossflag = 4;
    }
      
    if((myma < myboll_low_B) && (myma_pre > myboll_low_B_pre ) )
    {
        crossflag = -5;
    }
      
    if((myma > myboll_low_B) && (myma_pre < myboll_low_B_pre ) )
    {
        crossflag = -4; 
    }
  
    if((myma > myboll_mid_B) && (myma_pre < myboll_mid_B_pre ))
    {
        crossflag = 1;        
    } 
    if( (myma < myboll_mid_B) && (myma_pre > myboll_mid_B_pre ))
    {
        crossflag = -1;               
    }     
    
    if(0 !=   crossflag)    
    {


      MAFive=iMA(my_symbol,my_timeperiod,5,0,MODE_SMA,PRICE_CLOSE,i); 
      MAThentyOne=iMA(my_symbol,my_timeperiod,21,0,MODE_SMA,PRICE_CLOSE,i); 
      MASixty=iMA(my_symbol,my_timeperiod,60,0,MODE_SMA,PRICE_CLOSE,i); 

      //定义多空状态指标
      StrongWeak =0.5;

      if(MAFive > MAThentyOne)
      {     
        /*多均线多头向上*/
        if(MASixty < MAThentyOne)
        {
           StrongWeak =0.9;
        }
        else if ((MASixty >= MAThentyOne) &&(MASixty <MAFive))
        {
           StrongWeak =0.6;
        }
        else
        {
           StrongWeak =0.5;
        }
      
      }
      else if (MAFive < MAThentyOne)
      {
        /*多均线多头向下*/
        if(MASixty > MAThentyOne)
        {
           StrongWeak =0.1;
        }
        else if ((MASixty <= MAThentyOne) &&(MASixty > MAFive))
        {
           StrongWeak =0.4;
        }
        else
        {
           StrongWeak =0.5;
        }   
      
      }
      else
      {
        StrongWeak =0.5;

      }

      if((BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].BoolLengthFlag == HBOOLLENGTHNUMBER)
      &&(j == (HCROSSNUMBER)))
      {
      
         break;
      }      
      if(((crossflag == -1)||(crossflag == 1))&&(myboollength>0.0001))
      {
        if(BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].BoolLengthFlag < HBOOLLENGTHNUMBER) 
        {
          BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].BoolLength[BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].BoolLengthFlag] = myboollength;
          BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].BoolLengthFlag++;
        } 

      }
      if (j <= (HCROSSNUMBER-1))
      {
        BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossStrongWeakL[j] = StrongWeak;
        BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossFlagL[j] = crossflag;
        //BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossDatetime[j] = TimeCurrent() - i*Period()*60;
        BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossBoolPosL[j] = iBars(my_symbol,my_timeperiod)-i;
      }
      j++;

    }

  }


  if(BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].BoolLengthFlag > 0)
  {
    double  templength = 0;
    for (i = 0 ; i <(BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].BoolLengthFlag-1); i++)
    {
      templength += BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].BoolLength[i];
    }    
    BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].AverageBoolLength = templength/BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].BoolLengthFlag;

  }
  else
  {


    Print("InitcrossValue BoolLength Fatel error in all Zero!"+my_symbol+my_timeperiod);
    

  }


  
  return 0;

}



void InitOriginalOrder()
{

  int SymPos;
  int i ,j;
  string my_symbol;
  int buysellpoint;
  int subbuysellpoint;
  int magicnumber,NowMagicNumber;

  double MinValue3 = 100000;
  double MaxValue4=-1;
  int my_timeperiod;  
  my_timeperiod = timeperiod[0];

  double orderPrice,orderStopless;
  double vbid,vask;
  int vdigits;

  //当前订单参数导入
  for (i = 0; i < OrdersTotal(); i++)
  {
    if (OrderSelect(i,SELECT_BY_POS,MODE_TRADES))
    {   
      magicnumber = OrderMagicNumber();
      SymPos = ((int)magicnumber) /MAINMAGIC-1;
      NowMagicNumber = magicnumber - (SymPos+1)*MAINMAGIC;     
      if((SymPos>=0)&&(SymPos<symbolNum))
      {
        my_symbol = MySymbol[SymPos];

        subbuysellpoint = (NowMagicNumber%100);  
        if((subbuysellpoint>= 0)&&(subbuysellpoint< HSUBBUYSELLPOINTNUM))
        {
          buysellpoint = ((int)NowMagicNumber) /100;
          if((buysellpoint>=0)&&(buysellpoint<HBUYSELLALGNUM*3))
          {

            BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].opentime = OrderOpenTime();
            BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].openprice = OrderOpenPrice();

            BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].fakedstoploss = OrderStopLoss();
            BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].fakedtakeprofit = OrderTakeProfit();
            BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].takeprofit = OrderTakeProfit();
            BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoploss = OrderStopLoss();

            if((OrderType()==OP_BUY)||(OrderType()==OP_SELL))
            {
              //设置该订单状态为开仓状态
              BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].pendingstate = HPENDINGSTATEOPEN;   

            }
            else if((OrderType()==OP_BUYLIMIT)||(OrderType()==OP_SELLLIMIT))
            {

              //设置该订单状态为Stop类的挂单状态，处于该状态下的订单需要持续监控，但是开始时间设置为当前的一分钟ibar
              BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].pendingstate = HPENDINGSTATEHUNGLIMIT;                
              //记录当前一分钟的ibar位置
              BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].OneMOpenPos =   iBars(MySymbol[SymPos],timeperiod[0]);

            }

            else if((OrderType()==OP_BUYSTOP)||(OrderType()==OP_SELLSTOP))
            {

              //设置该订单状态为Stop类的挂单状态，处于该状态下的订单需要持续监控，但是开始时间设置为当前的一分钟ibar
              BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].pendingstate = HPENDINGSTATEHUNGSTOP;               
              //记录当前一分钟的ibar位置
              BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].OneMOpenPos =   iBars(MySymbol[SymPos],timeperiod[0]);

            }           
            else
            {
              ;
            }

            if(BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].my_timeperiod == PERIOD_M1)
            {
              BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoptailing = BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoptailtimes*BoolCrossRecord[SymPos][buysellpoint][4].AverageBoolLength;

            }
            else if(BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].my_timeperiod == PERIOD_M5)
            {
              BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoptailing = BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoptailtimes*BoolCrossRecord[SymPos][buysellpoint][5].AverageBoolLength;

            }
            else
            {
              BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoptailing = BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoptailtimes*BoolCrossRecord[SymPos][buysellpoint][5].AverageBoolLength;

            }





            vbid    = MarketInfo(my_symbol,MODE_BID);   
            vask    = MarketInfo(my_symbol,MODE_ASK);                       
            vdigits = (int)MarketInfo(my_symbol,MODE_DIGITS);   
            
            //买交易
            if(OrderType()==OP_BUY)
            {


              MaxValue4 = -1;
              orderPrice = vask;   
              if(BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].opentime >= iTime(my_symbol,my_timeperiod,0))
              {
                orderPrice = vask;                 
              }
              else
              {

                for (j= 0;j < iBars(my_symbol,my_timeperiod)-100;j++)
                {
                  if(MaxValue4 < iHigh(my_symbol,my_timeperiod,j))
                  {
                    MaxValue4 = iHigh(my_symbol,my_timeperiod,j);
                  }
                  if(BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].opentime >= iTime(my_symbol,my_timeperiod,j))
                  {
                    //Print("buy j = " + j+"ibar = " + iBars(my_symbol,my_timeperiod)); 
                    //j = iBars(my_symbol,my_timeperiod);
                    break;
                  }
                } 
                 orderPrice =   MaxValue4;

              }
      
       
              orderStopless = orderPrice - BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoptailing;   
              orderStopless = NormalizeDouble(orderStopless,vdigits);     


              //不扩大亏损额度，且平保
              if((orderStopless<OrderOpenPrice())
                &&(orderStopless > BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoploss))
              {

                //设置实际止损值
                BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoploss = orderStopless;

                Print(BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].MagicName + "InitOriginalOrder orderStopless stoptrailling Modify:"
                        +"orderPrice ="+OrderOpenPrice()+"orderStopless="+orderStopless);                  
                

              }
              //平保
              else if((orderStopless>OrderOpenPrice())&&((OrderOpenPrice() - BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoploss>0.000001)))
              {
                //设置
                orderStopless = OrderOpenPrice();
                orderStopless = NormalizeDouble(orderStopless,vdigits); 

                //≥设置实际止损值
                BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoploss = orderStopless;

                Print(BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].MagicName + "InitOriginalOrder orderStopless stoptrailling Modify:"
                        +"orderPrice ="+OrderOpenPrice()+"orderStopless="+orderStopless);                  
                
              }
              else
              {
                ;
              }

            }
            else if(OrderType()==OP_SELL)
            {


              MinValue3 = 100000;
              orderPrice = vbid;   
              if(BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].opentime >= iTime(my_symbol,my_timeperiod,0))
              {
                orderPrice = vbid;                 
              }
              else
              {

                for (j= 0;j < iBars(my_symbol,my_timeperiod)-100;j++)
                {
                  if(MinValue3 > iLow(my_symbol,my_timeperiod,j))
                  {
                    MinValue3 = iLow(my_symbol,my_timeperiod,j);
                  }
                  if(BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].opentime >= iTime(my_symbol,my_timeperiod,j))
                  {
                    //Print("buy j = " + j+"ibar = " + iBars(my_symbol,my_timeperiod)); 
                    //j = iBars(my_symbol,my_timeperiod);                                      
                    break;
                  }
                } 
                 orderPrice =   MinValue3;

              }
   
            
              orderStopless = orderPrice + BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoptailing;   
              orderStopless = NormalizeDouble(orderStopless,vdigits);     

              //不扩大亏损额度，且平保
              if((orderStopless>OrderOpenPrice())
                &&(orderStopless < BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoploss))
              {

                //设置实际止损值
                BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoploss = orderStopless;
                Print(BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].MagicName + "orderStopless stoptrailling Modify:"
                        +"orderPrice ="+OrderOpenPrice()+"orderStopless="+orderStopless);     

              } 
              //平保
              else if((orderStopless<OrderOpenPrice())&&((BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoploss - OrderOpenPrice())>0.000001))
              {
                //设置
                orderStopless = OrderOpenPrice();
                orderStopless = NormalizeDouble(orderStopless,vdigits); 

                //设置实际止损值
                BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoploss = orderStopless;

                Print(BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].MagicName + "orderStopless stoptrailling Modify:"
                        +"orderPrice ="+OrderOpenPrice()+"orderStopless="+orderStopless);     


              }
              else
              {
                ;
              }                 

            }
            else
            {
              ;
            }

            
            //BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoptailing = BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoptailtimes*(OrderOpenPrice()-OrderStopLoss())*BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].buysellflag;                                   

            Print(BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].MagicName+"OrderLoad:" + "openprice=" + OrderOpenPrice() +"OrderStopLoss ="
                  +OrderStopLoss()+"OrderTakeProfit="+OrderTakeProfit()+"stoptailing="+BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoptailing);  
          }
        }           
      }
    }
  }

}
// 初始化定义当前的MA强弱情况，由trend和strongweak分别定义
void InitMA(int SymPos,int buysellpoint,int timeperiodnum)
{

  double MAThree,MAFive,MAThen,MAThentyOne,MASixty;
  double MAThreePre,MAFivePre,MAThenPre,MAThentyOnePre,MASixtyPre;
  double MAThreePrePre,MAThenPrePre;
  double StrongWeak;
  int my_timeperiod;  
  string my_symbol;
  
  my_symbol = MySymbol[SymPos];
  my_timeperiod = timeperiod[timeperiodnum];  
  
  MAThree=iMA(my_symbol,my_timeperiod,3,0,MODE_SMA,PRICE_CLOSE,1); 
  MAThen=iMA(my_symbol,my_timeperiod,10,0,MODE_SMA,PRICE_CLOSE,1); 

  MAThreePre = iMA(my_symbol,my_timeperiod,3,0,MODE_SMA,PRICE_CLOSE,2); 
  MAThenPre=iMA(my_symbol,my_timeperiod,10,0,MODE_SMA,PRICE_CLOSE,2); 

  MAThreePrePre = iMA(my_symbol,my_timeperiod,3,0,MODE_SMA,PRICE_CLOSE,3); 
  MAThenPrePre=iMA(my_symbol,my_timeperiod,10,0,MODE_SMA,PRICE_CLOSE,3); 

  
  MAFive=iMA(my_symbol,my_timeperiod,5,0,MODE_SMA,PRICE_CLOSE,1); 
  MAThentyOne=iMA(my_symbol,my_timeperiod,21,0,MODE_SMA,PRICE_CLOSE,1); 
  MASixty=iMA(my_symbol,my_timeperiod,60,0,MODE_SMA,PRICE_CLOSE,1); 
 
  MAFivePre=iMA(my_symbol,my_timeperiod,5,0,MODE_SMA,PRICE_CLOSE,2); 
  MAThentyOnePre=iMA(my_symbol,my_timeperiod,21,0,MODE_SMA,PRICE_CLOSE,2); 
  MASixtyPre=iMA(my_symbol,my_timeperiod,60,0,MODE_SMA,PRICE_CLOSE,2); 
 



  //定义上升下降加速指标
 
  StrongWeak =0.5;
 

  if(((MAThree-MAThreePre) > (MAThen-MAThenPre))&&((MAThenPre-MAThenPrePre)<(MAThen-MAThenPre)))
  {   
    StrongWeak =0.9;  
  }
  if(((MAThree-MAThreePre) < (MAThen-MAThenPre))&&((MAThenPre-MAThenPrePre)>(MAThen-MAThenPre)))
  {
    StrongWeak =0.1;
  
  }
  else
  {
    StrongWeak =0.5;

  }

  //MoreTrend用来定义加速上涨或者加速下跌 
  BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].MoreTrend = StrongWeak;


  //定义上升下降指标
  StrongWeak =0.5;
 

  if((MAThree > MAThen)&&(MAThenPre<MAThen))
  {   
    StrongWeak =0.9;  
  }
  else if ((MAThree < MAThen)&&(MAThenPre>MAThen))
  {
    StrongWeak =0.1;
  
  }
  else
  {
    StrongWeak =0.5;

  }

  //Trend用来定义上涨，或者下跌趋势，非加速上涨或者加速下跌 
  BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].Trend = StrongWeak;

 
  //定义多空状态指标
  StrongWeak =0.5;

  if(MAFive > MAThentyOne)
  {     
    /*多均线多头向上*/
    if(MASixty < MAThentyOne)
    {
       StrongWeak =0.9;
    }
    else if ((MASixty >= MAThentyOne) &&(MASixty <MAFive))
    {
       StrongWeak =0.6;
    }
    else
    {
       StrongWeak =0.5;
    }
  
  }
  else if (MAFive < MAThentyOne)
  {
    /*多均线多头向下*/
    if(MASixty > MAThentyOne)
    {
       StrongWeak =0.1;
    }
    else if ((MASixty <= MAThentyOne) &&(MASixty > MAFive))
    {
       StrongWeak =0.4;
    }
    else
    {
       StrongWeak =0.5;
    }   
  
  }
  else
  {
    StrongWeak =0.5;

  }

  BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].StrongWeak = StrongWeak; 
  
}



// 定义穿越bool点标准差为2时的值、位置、强弱值，并且保留前一个穿越位置的值
void ChangeCrossValue( int mvalue,double  mstrongweak,int SymPos,int buysellpoint,int timeperiodnum,double bool_length)
{

  int i;
  int my_timeperiod;
  string symbol;
  symbol = MySymbol[SymPos];
  my_timeperiod = timeperiod[timeperiodnum];


    
  if (mvalue == BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossFlag[0])
  {
    BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossFlag[0] = mvalue;
  //  BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossDatetime[0] = TimeCurrent();
    BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossBoolPos[0] = iBars(symbol,my_timeperiod); 
    
    BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossStrongWeak[0] = mstrongweak;    
  
    
    return;
  }

    if(((mvalue == -1)||(mvalue == 1))&&(bool_length>0.0001))
    {
      if(BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].BoolLengthFlag < HBOOLLENGTHNUMBER) 
      {
        for (i = 0 ; i <(BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].BoolLengthFlag-1); i++)
        {
          BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].BoolLength[BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].BoolLengthFlag-1-i] 
            = BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].BoolLength[BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].BoolLengthFlag-2-i];

        }
        BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].BoolLengthFlag++;
        BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].BoolLength[0] = bool_length;

      } 
      else
      {
        for (i = 0 ; i <(BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].BoolLengthFlag-1); i++)
        {
          BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].BoolLength[HBOOLLENGTHNUMBER-1-i] 
            = BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].BoolLength[HBOOLLENGTHNUMBER-2-i];

        }
        BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].BoolLength[0] = bool_length;


      }

    }

  if(BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].BoolLengthFlag > 0)
  {
    double templength = 0;
    for (i = 0 ; i <(BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].BoolLengthFlag-1); i++)
    {
      templength += BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].BoolLength[i];
    }    
    BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].AverageBoolLength = templength/BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].BoolLengthFlag;

  }
  else
  {

    if(bool_length >0.0001)
    {
      BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].AverageBoolLength = bool_length;
      Print("ChangeCrossValue BoolLength Fatel error in previous Zero,keep current BoolLength!:"+symbol+my_timeperiod);

    }
    else
    {
      Print("ChangeCrossValue BoolLength Fatel error in all Zero!"+symbol+my_timeperiod);
    }

  }


  for (i = 0 ; i <(HCROSSNUMBER-1); i++)
  {
    BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossFlag[(HCROSSNUMBER-1)-i] = BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossFlag[(HCROSSNUMBER-2)-i];
  //  BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossDatetime[(HCROSSNUMBER-1)-i] = BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossDatetime[(HCROSSNUMBER-2)-i];
    BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossBoolPos[(HCROSSNUMBER-1)-i] = BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossBoolPos[(HCROSSNUMBER-2)-i] ;   
    BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossStrongWeak[(HCROSSNUMBER-1)-i] = BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossStrongWeak[(HCROSSNUMBER-2)-i];
  }
  
  BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossFlag[0] = mvalue;
  //BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossDatetime[0] = TimeCurrent();
  BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossBoolPos[0] = iBars(symbol,my_timeperiod);
  
  BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossStrongWeak[0] = mstrongweak;

  return;
}


// 定义穿越bool点标准差为1.7时的值、位置、强弱值，并且保留前一个穿越位置的值
void ChangeCrossValueL( int mvalue,double  mstrongweak,int SymPos,int buysellpoint,int timeperiodnum)
{

  int i;
  int my_timeperiod;
  string symbol;
    symbol = MySymbol[SymPos];
  my_timeperiod = timeperiod[timeperiodnum];

    
  if (mvalue == BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossFlagL[0])
  {
    BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossFlagL[0] = mvalue;
  //  BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossDatetime[0] = TimeCurrent();
    BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossBoolPosL[0] = iBars(symbol,my_timeperiod);  
    
    BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossStrongWeakL[0] = mstrongweak;   
    
    return;
  }
  for (i = 0 ; i <(HCROSSNUMBER-1); i++)
  {
    BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossFlagL[(HCROSSNUMBER-1)-i] = BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossFlagL[(HCROSSNUMBER-2)-i];
  //  BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossDatetime[(HCROSSNUMBER-1)-i] = BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossDatetime[(HCROSSNUMBER-2)-i];
    BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossBoolPosL[(HCROSSNUMBER-1)-i] = BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossBoolPosL[(HCROSSNUMBER-2)-i] ;   
    BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossStrongWeakL[(HCROSSNUMBER-1)-i] = BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossStrongWeakL[(HCROSSNUMBER-2)-i];
  }
  
  BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossFlagL[0] = mvalue;
  //BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossDatetime[0] = TimeCurrent();
  BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossBoolPosL[0] = iBars(symbol,my_timeperiod);
  
  BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossStrongWeakL[0] = mstrongweak;

  return;
}


/*非Openday期间不新开单*/
// 考虑了周六和周日的特俗情况，约束不大
bool opendaycheck(int SymPos)
{
  //  int i;
  string symbol;
  bool tradetimeflag;
  datetime timelocal;

  symbol = MySymbol[SymPos];
  tradetimeflag = true;

    
    timelocal = TimeCurrent() + globaltimezonediff*60*60;


  //  Print("opendaycheck:" + "timelocal=" + TimeToString(timelocal,TIME_DATE)
  //         +"timelocal=" + TimeToString(timelocal,TIME_SECONDS)); 

  //  Print("opendaycheck:" + "timecur=" + TimeToString(TimeCurrent(),TIME_DATE)
  //           +"timecur=" + TimeToString(TimeCurrent(),TIME_SECONDS)); 
    
          
  
  //周一早5点前不下单 
  if (TimeDayOfWeek(timelocal) == 1)
  {
    if (TimeHour(timelocal) < 5 ) 
    {
      tradetimeflag = false;
    }
  }
  
  //周六凌晨2点后不下单    
  if (TimeDayOfWeek(timelocal) == 6)
  {
    if (TimeHour(timelocal) > 2 )  
    {
      tradetimeflag = false;    
    }
  } 

  //周日不下单   
  if (TimeDayOfWeek(timelocal) == 0)
  {
      tradetimeflag = false;    
  }   
  return tradetimeflag;
}

/*欧美交易时间段多以趋势和趋势加强为主，非交易时间多以震荡为主，以此区分一些小周期的交易策略*/
/*因为三倍佣金的问题，周三的交易策略比较保守*/
bool tradetimecheck(int SymPos)
{
  //  int i;
  string symbol;
  bool tradetimeflag ;
  datetime timelocal; 
    symbol = MySymbol[SymPos];
  tradetimeflag = false;


    /*原则上采用服务器交易时间，为了便于人性化处理，做了一个转换*/ 
    timelocal = TimeCurrent() + globaltimezonediff*60*60;


  //13点前不做趋势单，主要针对1分钟线和五分钟线，非欧美时间趋势不明显，针对趋势突破单，要用这个来检测
  //最原始的是下午1点前不做趋势单，通过扩大止损来寻找更多机会

  if (TimeDayOfWeek(timelocal) == 3)
  { 
    /*周三为了规避三倍佣金问题，因此20点以后不交易*/
    if ((TimeHour(timelocal) >= 13 )&& (TimeHour(timelocal) <20 )) 
    {
      tradetimeflag = true;   
    } 
  }
  else
  {
    if ((TimeHour(timelocal) >= 13 )&& (TimeHour(timelocal) <22 )) 
    {
      tradetimeflag = true;   
    }     
    
  }
  /*测试期间全时间段交易*/
  tradetimeflag = true;   
  
  return tradetimeflag;
  
}

// exness外汇商显示的杠杆跟实际杠杆比是1:2，因此需要修正
int myaccountleverage()
{
  int leverage;
  
  
  leverage = AccountLeverage();
  
  /*规避exness实际显示杠杆错误的问题*/
  if(AccountServer() == HEXNESSSERVER)
  {
    leverage = leverage*2;
    
  }
  return leverage;
}

/*仓位检测，确保账户总余额可以交易4次以上*/
// 正常交易的全局交易开关关闭的情况下不交易
bool accountcheck()
{
  bool accountflag ;
  int leverage ;
  accountflag = true;
  leverage = myaccountleverage();
  if(leverage < 20)
  {
    Print("Account leverage is to low leverage = ",leverage);   
    accountflag = false;    
  }


  /*全局交易开关关闭的情况下不交易*/
  if(false == getglobaltradeflag())
  {
    //accountflag = false;
  }

  //账户低于100美金的时候直接交易
  if(AccountFreeMargin()<100)
  {
    accountflag = true;
  }

  return accountflag; 
  
}


int getfreesubbuysellnumber(int mySymPos,int mybuysellpoint)
{
  int i;
  int subbuysellpoint;

  int mysubbuysellpoint = -100;


  for(subbuysellpoint = 0; subbuysellpoint < BuySellPosRecord[mySymPos][mybuysellpoint][0].subbuysellnumber;subbuysellpoint++)
  {
    BuySellPosRecord[mySymPos][mybuysellpoint][subbuysellpoint].tradedflag = -1;

  }


  for (i = 0; i < OrdersTotal(); i++)
  {
    if (OrderSelect(i,SELECT_BY_POS,MODE_TRADES))
    {

      for(subbuysellpoint = 0; subbuysellpoint < BuySellPosRecord[mySymPos][mybuysellpoint][0].subbuysellnumber;subbuysellpoint++)
      {

        if(BuySellPosRecord[mySymPos][mybuysellpoint][subbuysellpoint].magicnumber == OrderMagicNumber())
        {

         if((OrderType()==OP_BUY)||(OrderType()==OP_SELL))
         {
             BuySellPosRecord[mySymPos][mybuysellpoint][subbuysellpoint].tradedflag = 1;
             break;
          }
        }
      }
      
    }
  }


  for(subbuysellpoint = 0; subbuysellpoint < BuySellPosRecord[mySymPos][mybuysellpoint][0].subbuysellnumber;subbuysellpoint++)
  {
    if(BuySellPosRecord[mySymPos][mybuysellpoint][subbuysellpoint].tradedflag < 0)
    {
      mysubbuysellpoint = subbuysellpoint;
      break;

    }

  }

  return mysubbuysellpoint;
}

int getfirsttradesubbuysellnumber(int mySymPos,int mybuysellpoint)
{
  int i;
  int subbuysellpoint;

  int mysubbuysellpoint = -100;


  for(subbuysellpoint = 0; subbuysellpoint < BuySellPosRecord[mySymPos][mybuysellpoint][0].subbuysellnumber;subbuysellpoint++)
  {
    BuySellPosRecord[mySymPos][mybuysellpoint][subbuysellpoint].tradedflag = -1;

  }


  for (i = 0; i < OrdersTotal(); i++)
  {
    if (OrderSelect(i,SELECT_BY_POS,MODE_TRADES))
    {

      for(subbuysellpoint = 0; subbuysellpoint < BuySellPosRecord[mySymPos][mybuysellpoint][0].subbuysellnumber;subbuysellpoint++)
      {

        if(BuySellPosRecord[mySymPos][mybuysellpoint][subbuysellpoint].magicnumber == OrderMagicNumber())
        {
         if((OrderType()==OP_BUY)||(OrderType()==OP_SELL))
         {
             BuySellPosRecord[mySymPos][mybuysellpoint][subbuysellpoint].tradedflag = 1;
             break;
          }
        }
      }
      
    }
  }


  for(subbuysellpoint = 0; subbuysellpoint < BuySellPosRecord[mySymPos][mybuysellpoint][0].subbuysellnumber;subbuysellpoint++)
  {
    if(BuySellPosRecord[mySymPos][mybuysellpoint][subbuysellpoint].tradedflag > 0)
    {
      mysubbuysellpoint = subbuysellpoint;
      break;

    }

  }

  return mysubbuysellpoint;
}




int checkpingbao(int mySymPos,int buysellflag)
{
  int i,SymPos,NowMagicNumber;
  string my_symbol;
  double vbid,vask;
  int ret = 1;
  int buysellpoint;
  int subbuysellpoint;

  for (i = 0; i < OrdersTotal(); i++)
  {
    if (OrderSelect(i,SELECT_BY_POS,MODE_TRADES))
    {
      if(isvalidmagicnumberall((int)OrderMagicNumber()) == true)
      {     

        SymPos = ((int)OrderMagicNumber()) /MAINMAGIC-1;
        NowMagicNumber = OrderMagicNumber() - (SymPos+1) *MAINMAGIC;

        buysellpoint = ((int)NowMagicNumber) /100;        
        subbuysellpoint = (NowMagicNumber%100);   
          
        my_symbol = MySymbol[SymPos];
        
        vbid    = MarketInfo(my_symbol,MODE_BID);             
        vask    = MarketInfo(my_symbol,MODE_ASK); 

        if(((buysellpoint>=0)&&(buysellpoint<3*HBUYSELLALGNUM))&&(SymPos == mySymPos))
        {

          //当去掉止盈的时候，程序对该单放弃监控，转为手动监控，通常是指那些基本面同步发生了重大同方向的变化，且适合长期持有的单子；改为手工持单
          //一般情况下不触发
          if(OrderTakeProfit()>0.01)
          {
            //if((TimeCurrent()-OrderOpenTime())>BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].keepperiod)
            {
              if((OrderType()==OP_BUY)&&(buysellflag == 1))
              {

                //存在任意一单未平保的情况下不再开单，确保不会出现大量亏损单
                if((OrderOpenPrice() < (BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoploss-0.000001))||
                  (OrderOpenPrice() > (BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoploss+0.000001)))
                {
                  //Print(BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].MagicName+"OrderOpenPrice ="+OrderOpenPrice()
                  //  +"；stoploss=" +BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoploss+"checkpingbao pingbao error" );  
                    ret = -1;            
                  return ret;
                }            
              }
              
              if((OrderType()==OP_SELL)&&(buysellflag == -1))
              {
                //存在任意一单未平保的情况下不再开单，确保不会出现大量亏损单
                if((OrderOpenPrice() < (BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoploss-0.000001))||
                  (OrderOpenPrice() > (BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoploss+0.000001)))
                {
                  //Print(BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].MagicName+"OrderOpenPrice ="+OrderOpenPrice()
                  //  +"；stoploss=" +BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoploss+"checkpingbao pingbao error" );  
                    ret = -1;            
                  return ret;
                }           
            
              }

            }

          }   
        }   
      
      }
      
    }
  }
  
  return ret;
}





int getlatestsubbuysellopentime(int mySymPos,int mybuysellpoint)
{
  int i;
  int subbuysellpoint;

  int latestopentime = 0;


  for (i = 0; i < OrdersTotal(); i++)
  {
    if (OrderSelect(i,SELECT_BY_POS,MODE_TRADES))
    {

      for(subbuysellpoint = 0; subbuysellpoint < HSUBBUYSELLPOINTNUM;subbuysellpoint++)
      {

        if(BuySellPosRecord[mySymPos][mybuysellpoint][subbuysellpoint].magicnumber == OrderMagicNumber())
        {

          if(OrderOpenTime() > latestopentime)
          {
            latestopentime = OrderOpenTime();
          }

        }
      }
      
    }
  }

  return latestopentime;
}


////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////
// 正常交易有效magicnumber，判断因素包括有效外汇、有效时间周一-周五、正常交易买卖点1-10
// 后面改进方式为将出现快速大幅变化的产生大幅盈利的交易排除在该交易点之外，处理起来比较复杂；也就是池塘捞到的大鱼要持续持有。
// 其中一个做法是如果当前大幅止损价格已经盈利，或者已经设置了大幅止损价格盈利的止损点。
bool isvalidmagicnumbertrend(int magicnumber)
{
    
  bool flag = true;
  int SymPos,NowMagicNumber;
  
  SymPos = ((int)magicnumber) /MAINMAGIC-1;
  NowMagicNumber = magicnumber - (SymPos+1) *MAINMAGIC;

  if((SymPos<0)||(SymPos>=symbolNum))
  {
    flag = false;
  } 
  
  //周一到周五的单子
  if((HSUBBUYSELLPOINTNUM<=(NowMagicNumber%100))||(0 > (NowMagicNumber%100)))
  {
    flag = false;
  } 
  

  NowMagicNumber = ((int)NowMagicNumber) /100;
  if((NowMagicNumber<0)||(NowMagicNumber>=HBUYSELLALGNUM*3))
  {
    flag = false;
  } 
  
  //flag = true;

  return flag;
  
}

//////////////////////////////////////////////////

// 某个外汇买单的所有开单的最大值
double orderbuymaxopenpricetrend(int mySymPos)
{
  int count = 0;
  int i,SymPos,NowMagicNumber;
  string my_symbol;
  double vbid,vask;
  double maxvalue = -100000000;

  int buysellpoint;
  int subbuysellpoint;

  for (i = 0; i < OrdersTotal(); i++)
  {
    if (OrderSelect(i,SELECT_BY_POS,MODE_TRADES))
    {
      if(isvalidmagicnumbertrend((int)OrderMagicNumber()) == true)
      {     

        SymPos = ((int)OrderMagicNumber()) /MAINMAGIC-1;
        NowMagicNumber = OrderMagicNumber() - (SymPos+1) *MAINMAGIC;

        buysellpoint = ((int)NowMagicNumber) /100;        
        subbuysellpoint = (NowMagicNumber%100);   
          
        my_symbol = MySymbol[SymPos];
        
        vbid    = MarketInfo(my_symbol,MODE_BID);             
        vask    = MarketInfo(my_symbol,MODE_ASK); 

        if((buysellpoint>=0)&&(buysellpoint<3*HBUYSELLALGNUM))
        {
          if(mySymPos == SymPos)
          {
            //当去掉止盈的时候，程序对该单放弃监控，转为手动监控，通常是指那些基本面同步发生了重大同方向的变化，且适合长期持有的单子；改为手工持单
            //一般情况下不触发
            if(OrderTakeProfit()>0.01)
            {
              //if((TimeCurrent()-OrderOpenTime())>(BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].keepperiod/10))
              {

                if((OrderType()==OP_BUY)||(OrderType()==OP_BUY))
                {

                  if(OrderOpenPrice()>maxvalue)
                  {
                    maxvalue = OrderOpenPrice();;
                  } 

                }

              }

            }       
          }
        }
      
      }
      
    }
  }
  
  return maxvalue;
}


// 某个外汇买单所有开单的最小值
double orderbuyminopenpricetrend(int mySymPos)
{
  int count = 0;
  int i,SymPos,NowMagicNumber;
  string my_symbol;
  double vbid,vask;
  double minvalue = 100000000;

  int buysellpoint;
  int subbuysellpoint;

  for (i = 0; i < OrdersTotal(); i++)
  {
    if (OrderSelect(i,SELECT_BY_POS,MODE_TRADES))
    {
      if(isvalidmagicnumbertrend((int)OrderMagicNumber()) == true)
      {     

        SymPos = ((int)OrderMagicNumber()) /MAINMAGIC-1;
        NowMagicNumber = OrderMagicNumber() - (SymPos+1) *MAINMAGIC;

        buysellpoint = ((int)NowMagicNumber) /100;        
        subbuysellpoint = (NowMagicNumber%100);   
          
        my_symbol = MySymbol[SymPos];
        
        vbid    = MarketInfo(my_symbol,MODE_BID);             
        vask    = MarketInfo(my_symbol,MODE_ASK); 

        if((buysellpoint>=0)&&(buysellpoint<3*HBUYSELLALGNUM))
        {
          if(mySymPos == SymPos)
          {
            //当去掉止盈的时候，程序对该单放弃监控，转为手动监控，通常是指那些基本面同步发生了重大同方向的变化，且适合长期持有的单子；改为手工持单
            //一般情况下不触发
            if(OrderTakeProfit()>0.01)
            {
              //if((TimeCurrent()-OrderOpenTime())>(BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].keepperiod/10))
              {

                if((OrderType()==OP_BUY)||(OrderType()==OP_BUY))
                {

                  if(OrderOpenPrice()<minvalue)
                  {
                    minvalue = OrderOpenPrice();
                  } 

                }

              }

            }       
          }
        }
      
      }
      
    }
  }
  
  return minvalue;
}


// 某个外汇卖单的所有开单的最大值
double ordersellmaxopenpricetrend(int mySymPos)
{
  int count = 0;
  int i,SymPos,NowMagicNumber;
  string my_symbol;
  double vbid,vask;
  double maxvalue = -100000000;

  int buysellpoint;
  int subbuysellpoint;

  for (i = 0; i < OrdersTotal(); i++)
  {
    if (OrderSelect(i,SELECT_BY_POS,MODE_TRADES))
    {
      if(isvalidmagicnumbertrend((int)OrderMagicNumber()) == true)
      {     

        SymPos = ((int)OrderMagicNumber()) /MAINMAGIC-1;
        NowMagicNumber = OrderMagicNumber() - (SymPos+1) *MAINMAGIC;

        buysellpoint = ((int)NowMagicNumber) /100;        
        subbuysellpoint = (NowMagicNumber%100);   
          
        my_symbol = MySymbol[SymPos];
        
        vbid    = MarketInfo(my_symbol,MODE_BID);             
        vask    = MarketInfo(my_symbol,MODE_ASK); 

        if((buysellpoint>=0)&&(buysellpoint<3*HBUYSELLALGNUM))
        {
          if(mySymPos == SymPos)
          {
            //当去掉止盈的时候，程序对该单放弃监控，转为手动监控，通常是指那些基本面同步发生了重大同方向的变化，且适合长期持有的单子；改为手工持单
            //一般情况下不触发
            if(OrderTakeProfit()>0.01)
            {
              //if((TimeCurrent()-OrderOpenTime())>(BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].keepperiod/10))
              {

                if((OrderType()==OP_SELL)||(OrderType()==OP_SELL))
                {

                  if(OrderOpenPrice()>maxvalue)
                  {
                    maxvalue = OrderOpenPrice();;
                  } 

                }

              }

            }       
          }
        }
        
      }
      
    }
  }
  
  return maxvalue;
}


// 某个外汇卖单所有开单的最小值
double ordersellminopenpricetrend(int mySymPos)
{
  int count = 0;
  int i,SymPos,NowMagicNumber;
  string my_symbol;
  double vbid,vask;
  double minvalue = 100000000;

  int buysellpoint;
  int subbuysellpoint;

  for (i = 0; i < OrdersTotal(); i++)
  {
    if (OrderSelect(i,SELECT_BY_POS,MODE_TRADES))
    {
      if(isvalidmagicnumbertrend((int)OrderMagicNumber()) == true)
      {     

        SymPos = ((int)OrderMagicNumber()) /MAINMAGIC-1;
        NowMagicNumber = OrderMagicNumber() - (SymPos+1) *MAINMAGIC;

        buysellpoint = ((int)NowMagicNumber) /100;        
        subbuysellpoint = (NowMagicNumber%100);   
          
        my_symbol = MySymbol[SymPos];
        
        vbid    = MarketInfo(my_symbol,MODE_BID);             
        vask    = MarketInfo(my_symbol,MODE_ASK); 

        if((buysellpoint>=0)&&(buysellpoint<3*HBUYSELLALGNUM))
        {
          if(mySymPos == SymPos)
          {
            //当去掉止盈的时候，程序对该单放弃监控，转为手动监控，通常是指那些基本面同步发生了重大同方向的变化，且适合长期持有的单子；改为手工持单
            //一般情况下不触发
            if(OrderTakeProfit()>0.01)
            {
              //if((TimeCurrent()-OrderOpenTime())>(BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].keepperiod/10))
              {

                if((OrderType()==OP_SELL)||(OrderType()==OP_SELL))
                {

                  if(OrderOpenPrice()<minvalue)
                  {
                    minvalue = OrderOpenPrice();
                  } 

                }

              }

            }       
          }
        }
      
      }
      
    }
  }
  
  return minvalue;
}


/////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////


/////////////////////////////////////////////////////
bool isvalidmagicnumberall(int magicnumber)
{
    
  bool flag = true;
  int SymPos,NowMagicNumber;
  
  SymPos = ((int)magicnumber) /MAINMAGIC-1;
  NowMagicNumber = magicnumber - (SymPos+1) *MAINMAGIC;

  if((SymPos<0)||(SymPos>=symbolNum))
  {
    flag = false;
  } 
  
  //周一到周五的单子
  if((HSUBBUYSELLPOINTNUM<=(NowMagicNumber%100))||(0 > (NowMagicNumber%100)))
  {
    flag = false;
  } 
  

  NowMagicNumber = ((int)NowMagicNumber) /100;
  if((NowMagicNumber<0)||(NowMagicNumber>=HBUYSELLALGNUM*3))
  {
    flag = false;
  } 
  
  //flag = true;

  return flag;
  
}




///////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////

// 主程序初始化
int init()
{

  int SymPos;
  int timeperiodnum;
  string my_symbol;
  int symbolvalue;
  int buysellpoint;

  string MailTitlle ="";

  symbolvalue = 0;

  // 判断链接的外汇服务器是否正确
  if(false == forexserverconnect())
  {
    
    Print("connect to wrong server,and disable autotrade");     
    /*关闭自动交易*/
    return -1;
    
  } 
  else
  {
    Print("connect to right server,and enable autotrade");      
    /*打开自动交易*/
    //return 0;   
  }


  //设置全局变量，定义是否开启趋势买卖点、趋势转折买卖点、趋势转折挂单买卖点；通过修改全局变量可以在线改变
  initglobaltrig();
  
  // 初始化外汇集合，打开该外汇服务器上所有可能参加外汇运算的外汇对，具体哪些外汇参加运算，根据后面注册的不同算法来确定
  initsymbolall();  

  // 打开外汇集合
  openallsymbo();

  //初始化外汇特性参数
  initforexindex(); 

  // 初始化magicnumber
  //initmagicnumber();
  
  // 初始化时间周期
  inittiimeperiod();
  
  /*初始化正常交易全局交易指标，交易时间段使能，非交易时间段禁止*/
  initglobaltradeflag();  

  
  // 初始化买卖点的位置，
  InitBuySellPos();

  //算法注册初始化，所有的算法初始化函数的注册入口
  RegistAlgorithmInit();
  
  // 防止错误导致的重复交易
  Freq_Count = 0;
  TwentyS_Freq = 0;
  OneM_Freq = 0;
  ThirtyS_Freq = 0;
  FiveM_Freq = 0;
  ThirtyM_Freq = 0;
  
  for(SymPos = 0; SymPos < symbolNum;SymPos++)
  {

    for(buysellpoint = 0; buysellpoint < 3*HBUYSELLALGNUM;buysellpoint++)
    {

      for(timeperiodnum = 0; timeperiodnum < TimePeriodNum;timeperiodnum++)
      { 

        //my_symbol =   MySymbol[SymPos];
        //my_timeperiod = timeperiod[timeperiodnum];       

        // 初始化外汇集、周期集下的穿越bool集合
        InitcrossValue(SymPos,buysellpoint,timeperiodnum);
        // 初始化当前外汇、周期下的短期强弱trend和多头强弱
        InitMA(SymPos,buysellpoint,timeperiodnum);

      }

      if(BuySellPosRecord[SymPos][buysellpoint][0].algorithmflag == ALGORITHMFLAGENABLE)
      {
        Print(my_symbol+"AverageBoolLength["+SymPos+"]["+buysellpoint+"]2 = "+ (BoolCrossRecord[SymPos][buysellpoint][2].AverageBoolLength/BoolCrossRecord[SymPos][buysellpoint][0].AverageBoolLength));   
        Print(my_symbol+"AverageBoolLength["+SymPos+"]["+buysellpoint+"]4 = "+ (BoolCrossRecord[SymPos][buysellpoint][4].AverageBoolLength/BoolCrossRecord[SymPos][buysellpoint][0].AverageBoolLength));
     
      }
  
    }
  
  }

  InitOriginalOrder();
  // 打印账户信息情况
  Print("Server name is ", AccountServer());    
  Print("Account #",AccountNumber(), " leverage is ", AccountLeverage());
  Print("Account Balance= ",AccountBalance());    
  Print("Account free margin = ",AccountFreeMargin());    

  return 0;
  
}


// 主程序退出
int deinit()
{
  deinitglobaltrig();
  return 0;
}

int ChartEvent = 0;
bool PrintFlag = false;

// 每个时间周期调用一次，计算当前周期强弱等相关值，寻找bool穿越点，并记录当时的值
void calculateindicator()
{
  
  int SymPos;
  int timeperiodnum;
  int my_timeperiod;

  double ma;
  double boll_up_B,boll_low_B,boll_mid_B,bool_length;
  
  double MAThree,MAFive,MAThen,MAThentyOne,MASixty;
  double MAThreePre,MAFivePre,MAThenPre,MAThentyOnePre,MASixtyPre;
  double MAThreePrePre,MAThenPrePre;  
  double StrongWeak;
  double vbid,vask; 
  string my_symbol;
  double boolindex;
  int  buysellpoint;
  int crossflag;  

  for(SymPos = 0; SymPos < symbolNum;SymPos++)
  { 
    for(buysellpoint = 0; buysellpoint < 3*HBUYSELLALGNUM;buysellpoint++)
    {    
      if(BuySellPosRecord[SymPos][buysellpoint][0].algorithmflag == ALGORITHMFLAGENABLE)
      {
        for(timeperiodnum = 0; timeperiodnum < TimePeriodNum;timeperiodnum++)
        {
          
          my_symbol =   MySymbol[SymPos];
          my_timeperiod = timeperiod[timeperiodnum];      
          //确保指标计算是每个周期计算一次，而不是每个tick计算一次
          if ( ForexTimePeriod[SymPos][timeperiodnum].ChartEvent != iBars(my_symbol,my_timeperiod))
          {
            
            ma=iMA(my_symbol,my_timeperiod,BuySellPosRecord[SymPos][buysellpoint][0].Move_Av,0,MODE_SMA,PRICE_CLOSE,1); 
            // ma = Close[0];  
            boll_up_B = iBands(my_symbol,my_timeperiod,BuySellPosRecord[SymPos][buysellpoint][0].iBoll_B,BuySellPosRecord[SymPos][buysellpoint][0].iBool_Len,0,PRICE_CLOSE,MODE_UPPER,1);   
            boll_low_B = iBands(my_symbol,my_timeperiod,BuySellPosRecord[SymPos][buysellpoint][0].iBoll_B,BuySellPosRecord[SymPos][buysellpoint][0].iBool_Len,0,PRICE_CLOSE,MODE_LOWER,1);
            boll_mid_B = (boll_up_B + boll_low_B )/2;
            /*point*/
            bool_length =(boll_up_B - boll_low_B )/2;
      
            ma_pre = iMA(my_symbol,my_timeperiod,BuySellPosRecord[SymPos][buysellpoint][0].Move_Av,0,MODE_SMA,PRICE_CLOSE,2); 
            boll_up_B_pre = iBands(my_symbol,my_timeperiod,BuySellPosRecord[SymPos][buysellpoint][0].iBoll_B,BuySellPosRecord[SymPos][buysellpoint][0].iBool_Len,0,PRICE_CLOSE,MODE_UPPER,2);      
            boll_low_B_pre = iBands(my_symbol,my_timeperiod,BuySellPosRecord[SymPos][buysellpoint][0].iBoll_B,BuySellPosRecord[SymPos][buysellpoint][0].iBool_Len,0,PRICE_CLOSE,MODE_LOWER,2);
            boll_mid_B_pre = (boll_up_B_pre + boll_low_B_pre )/2;
      
            crossflag = 0;
            
          
            StrongWeak = BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].StrongWeak;
            
            /*本周期突破高点，观察如小周期未衰竭可追高买入，或者等待回调买入*/
            /*原则上突破bool线属于偏离价值方向太大，是要回归价值中枢的*/
            if((ma >boll_up_B) && (ma_pre < boll_up_B_pre ) )
            {
            
              crossflag = 5;    
              ChangeCrossValue(crossflag,StrongWeak,SymPos,buysellpoint,timeperiodnum,bool_length);
              //  Print(mMailTitlle + Symbol()+"::本周期突破高点，除(1M、5M周期bool口收窄且快速突破追高，移动止损），其他情况择机反向做空:"
              //  + DoubleToString(bool_length)+":"+DoubleToString(bool_length/Point));             
      
            }
            
            /*本周期突破高点后回调，观察如小周期长时间筑顶，寻机卖出*/
            else if((ma <boll_up_B) && (ma_pre > boll_up_B_pre ) )
            {
              crossflag = 4;
              ChangeCrossValue(crossflag,StrongWeak,SymPos,buysellpoint,timeperiodnum,bool_length);
              //   Print(mMailTitlle + Symbol()+"::本周期突破高点后回调，观察小周期如长时间筑顶，寻机做空:"
              //   + DoubleToString(bool_length)+":"+DoubleToString(bool_length/Point));              
      
           
            }
              
            
            /*本周期突破低点，观察如小周期未衰竭可追低卖出，或者等待回调卖出*/
            else if((ma < boll_low_B) && (ma_pre > boll_low_B_pre ) )
            {
            
              
              crossflag = -5;
              ChangeCrossValue(crossflag,StrongWeak,SymPos,buysellpoint,timeperiodnum,bool_length);
              //   Print(mMailTitlle + Symbol() + "::本周期突破低点，除(条件：1M、5M周期bool口收窄且快速突破追低，移动止损），其他情况择机反向做多:"
              //   + DoubleToString(bool_length)+":"+DoubleToString(bool_length/Point));                              
      
           
            }
              
            /*本周期突破低点后回调，观察如长时间筑底，寻机买入*/
            else if((ma > boll_low_B) && (ma_pre < boll_low_B_pre ) )
            {
              crossflag = -4; 
              ChangeCrossValue(crossflag,StrongWeak,SymPos,buysellpoint,timeperiodnum,bool_length);
              //   Print(mMailTitlle + Symbol() + "::本周期突破低点后回调，观察如小周期长时间筑底，寻机买入:"
              //   + DoubleToString(bool_length)+":"+DoubleToString(bool_length/Point));                              
      
      
            }
          
            /*本周期上穿中线，表明本周期趋势开始发生变化为上升，在下降大趋势下也可能是回调杀入机会*/
            else if((ma > boll_mid_B) && (ma_pre < boll_mid_B_pre ))
            {
            
              crossflag = 1;        
              ChangeCrossValue(crossflag,StrongWeak,SymPos,buysellpoint,timeperiodnum,bool_length);      
              //    Print(mMailTitlle + Symbol() + "::本周期上穿中线变化为上升，大周期下降大趋势下可能是回调做空机会："
              //    + DoubleToString(bool_length)+":"+DoubleToString(bool_length/Point));                             
      
         
            } 
            /*本周期下穿中线，表明趋势开始发生变化，在上升大趋势下也可能是回调杀入机会*/
            else if( (ma < boll_mid_B) && (ma_pre > boll_mid_B_pre ))
            {
              crossflag = -1;               
              ChangeCrossValue(crossflag,StrongWeak,SymPos,buysellpoint,timeperiodnum,bool_length);      
               //     Print(mMailTitlle + Symbol() + "::本周期下穿中线变化为下降，大周期上升大趋势下可能是回调做多机会："
               //     + DoubleToString(bool_length)+":"+DoubleToString(bool_length/Point));                             
      
            }             
            else
            {
               crossflag = 0;   
             
            }
      
            BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].BoolFlag = BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossFlag[0];
            BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossFlagChange = crossflag;


            ////////////////////////////////////////////////////////////////////////////
            
            ma=iMA(my_symbol,my_timeperiod,BuySellPosRecord[SymPos][buysellpoint][0].Move_Av,0,MODE_SMA,PRICE_CLOSE,1); 
            // ma = Close[0];  
            boll_up_B = iBands(my_symbol,my_timeperiod,BuySellPosRecord[SymPos][buysellpoint][0].iBoll_B,BuySellPosRecord[SymPos][buysellpoint][0].iBool_LenL,0,PRICE_CLOSE,MODE_UPPER,1);   
            boll_low_B = iBands(my_symbol,my_timeperiod,BuySellPosRecord[SymPos][buysellpoint][0].iBoll_B,BuySellPosRecord[SymPos][buysellpoint][0].iBool_LenL,0,PRICE_CLOSE,MODE_LOWER,1);
            boll_mid_B = (boll_up_B + boll_low_B )/2;
            /*point*/
            //bool_length =(boll_up_B - boll_low_B )/2;
      
            ma_pre = iMA(my_symbol,my_timeperiod,BuySellPosRecord[SymPos][buysellpoint][0].Move_Av,0,MODE_SMA,PRICE_CLOSE,2); 
            boll_up_B_pre = iBands(my_symbol,my_timeperiod,BuySellPosRecord[SymPos][buysellpoint][0].iBoll_B,BuySellPosRecord[SymPos][buysellpoint][0].iBool_LenL,0,PRICE_CLOSE,MODE_UPPER,2);      
            boll_low_B_pre = iBands(my_symbol,my_timeperiod,BuySellPosRecord[SymPos][buysellpoint][0].iBoll_B,BuySellPosRecord[SymPos][buysellpoint][0].iBool_LenL,0,PRICE_CLOSE,MODE_LOWER,2);
            boll_mid_B_pre = (boll_up_B_pre + boll_low_B_pre )/2;
      
            crossflag = 0;
                  
            StrongWeak = BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].StrongWeak;
            
            /*本周期突破高点，观察如小周期未衰竭可追高买入，或者等待回调买入*/
            /*原则上突破bool线属于偏离价值方向太大，是要回归价值中枢的*/
            if((ma >boll_up_B) && (ma_pre < boll_up_B_pre ) )
            {
            
              crossflag = 5;    
              ChangeCrossValueL(crossflag,StrongWeak,SymPos,buysellpoint,timeperiodnum);
              //  Print(mMailTitlle + Symbol()+"::本周期突破高点，除(1M、5M周期bool口收窄且快速突破追高，移动止损），其他情况择机反向做空:"
              //  + DoubleToString(bool_length)+":"+DoubleToString(bool_length/Point));             
      
            }
            
            /*本周期突破高点后回调，观察如小周期长时间筑顶，寻机卖出*/
            else if((ma <boll_up_B) && (ma_pre > boll_up_B_pre ) )
            {
              crossflag = 4;
              ChangeCrossValueL(crossflag,StrongWeak,SymPos,buysellpoint,timeperiodnum);
              //   Print(mMailTitlle + Symbol()+"::本周期突破高点后回调，观察小周期如长时间筑顶，寻机做空:"
              //   + DoubleToString(bool_length)+":"+DoubleToString(bool_length/Point));              
      
           
            }
              
            
            /*本周期突破低点，观察如小周期未衰竭可追低卖出，或者等待回调卖出*/
            else if((ma < boll_low_B) && (ma_pre > boll_low_B_pre ) )
            {
            
              
              crossflag = -5;
              ChangeCrossValueL(crossflag,StrongWeak,SymPos,buysellpoint,timeperiodnum);
              //   Print(mMailTitlle + Symbol() + "::本周期突破低点，除(条件：1M、5M周期bool口收窄且快速突破追低，移动止损），其他情况择机反向做多:"
              //   + DoubleToString(bool_length)+":"+DoubleToString(bool_length/Point));                              
      
           
            }
              
            /*本周期突破低点后回调，观察如长时间筑底，寻机买入*/
            else if((ma > boll_low_B) && (ma_pre < boll_low_B_pre ) )
            {
              crossflag = -4; 
              ChangeCrossValueL(crossflag,StrongWeak,SymPos,buysellpoint,timeperiodnum);
              //   Print(mMailTitlle + Symbol() + "::本周期突破低点后回调，观察如小周期长时间筑底，寻机买入:"
              //   + DoubleToString(bool_length)+":"+DoubleToString(bool_length/Point));                              
      
      
            }
          
            /*本周期上穿中线，表明本周期趋势开始发生变化为上升，在下降大趋势下也可能是回调杀入机会*/
            else if((ma > boll_mid_B) && (ma_pre < boll_mid_B_pre ))
            {
            
              crossflag = 1;        
              ChangeCrossValueL(crossflag,StrongWeak,SymPos,buysellpoint,timeperiodnum);     
              //    Print(mMailTitlle + Symbol() + "::本周期上穿中线变化为上升，大周期下降大趋势下可能是回调做空机会："
              //    + DoubleToString(bool_length)+":"+DoubleToString(bool_length/Point));                             
      
         
            } 
            /*本周期下穿中线，表明趋势开始发生变化，在上升大趋势下也可能是回调杀入机会*/
            else if( (ma < boll_mid_B) && (ma_pre > boll_mid_B_pre ))
            {
              crossflag = -1;               
              ChangeCrossValueL(crossflag,StrongWeak,SymPos,buysellpoint,timeperiodnum);     
               //     Print(mMailTitlle + Symbol() + "::本周期下穿中线变化为下降，大周期上升大趋势下可能是回调做多机会："
               //     + DoubleToString(bool_length)+":"+DoubleToString(bool_length/Point));                             
      
            }             
            else
            {
               crossflag = 0;   
             
            }
      
            BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].BoolFlagL = BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossFlag[0];
            BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossFlagChangeL = crossflag;
            
            
            
            
            //////////////////////////////////////////////////////////////////////////////


            
            vask    = MarketInfo(my_symbol,MODE_ASK);
            vbid    = MarketInfo(my_symbol,MODE_BID); 
            if(((bool_length <0.00001)&&(bool_length >=0))||((bool_length >-0.00001)&&(bool_length <0)) )
            {
              Print(my_symbol+":"+my_timeperiod+"bool_length is Zero,ERROR!!");
            }     
            else
            {
              boolindex = ((vask + vbid)/2 - boll_mid_B)/bool_length;
              BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].BoolIndex = boolindex;
            }
      
           
           
            MAThree=iMA(my_symbol,my_timeperiod,3,0,MODE_SMA,PRICE_CLOSE,1); 
            MAThen=iMA(my_symbol,my_timeperiod,10,0,MODE_SMA,PRICE_CLOSE,1);  

            MAThreePre = iMA(my_symbol,my_timeperiod,3,0,MODE_SMA,PRICE_CLOSE,2); 
            MAThenPre=iMA(my_symbol,my_timeperiod,10,0,MODE_SMA,PRICE_CLOSE,2); 

            MAThreePrePre = iMA(my_symbol,my_timeperiod,3,0,MODE_SMA,PRICE_CLOSE,3); 
            MAThenPrePre=iMA(my_symbol,my_timeperiod,10,0,MODE_SMA,PRICE_CLOSE,3); 
         
              
            MAFive=iMA(my_symbol,my_timeperiod,5,0,MODE_SMA,PRICE_CLOSE,1); 
            MAThentyOne=iMA(my_symbol,my_timeperiod,21,0,MODE_SMA,PRICE_CLOSE,1); 
            MASixty=iMA(my_symbol,my_timeperiod,60,0,MODE_SMA,PRICE_CLOSE,1); 
           
            MAFivePre=iMA(my_symbol,my_timeperiod,5,0,MODE_SMA,PRICE_CLOSE,2); 
            MAThentyOnePre=iMA(my_symbol,my_timeperiod,21,0,MODE_SMA,PRICE_CLOSE,2); 
            MASixtyPre=iMA(my_symbol,my_timeperiod,60,0,MODE_SMA,PRICE_CLOSE,2); 
             

            //定义上升下降加速指标
           
            StrongWeak =0.5;
           

            if(((MAThree-MAThreePre) > (MAThen-MAThenPre))&&((MAThenPre-MAThenPrePre)<(MAThen-MAThenPre)))
            {   
              StrongWeak =0.9;  
            }
            if(((MAThree-MAThreePre) < (MAThen-MAThenPre))&&((MAThenPre-MAThenPrePre)>(MAThen-MAThenPre)))
            {
              StrongWeak =0.1;
            
            }
            else
            {
              StrongWeak =0.5;

            }

            //MoreTrend用来定义加速上涨或者加速下跌 
            BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].MoreTrend = StrongWeak;

      
            StrongWeak =0.5;
           
      
            if((MAThree > MAThen)&&(MAThenPre<MAThen))
            {   
              StrongWeak =0.9;  
            }
            else if ((MAThree < MAThen)&&(MAThenPre>MAThen))
            {
              StrongWeak =0.1;
            
            }
            else
            {
              StrongWeak =0.5;
      
            }
      
            
            BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].Trend = StrongWeak;
            
      
             
             
             
            StrongWeak =0.5;
           
            if(MAFive > MAThentyOne)
            {
                
              /*多均线多头向上*/
              if(MASixty < MAThentyOne)
              {
                 StrongWeak =0.9;
              }
              else if ((MASixty >= MAThentyOne) &&(MASixty <MAFive))
              {
                 StrongWeak =0.6;
              }
              else
              {
                 StrongWeak =0.5;
              }
            
            }
            else if (MAFive < MAThentyOne)
            {
              /*多均线多头向下*/
              if(MASixty > MAThentyOne)
              {
                 StrongWeak =0.1;
              }
              else if ((MASixty <= MAThentyOne) &&(MASixty > MAFive))
              {
                 StrongWeak =0.4;
              }
              else
              {
                 StrongWeak =0.5;
              }   
            
            }
            else
            {
              StrongWeak =0.5;
           
            }
           
           
            BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].StrongWeak = StrongWeak;
      
           
      
        
          }
        } 

      }

    }

  } 
  


  return;
}


double myabs(double a)
{

  if(a < 0)
  {
    a =-a;
  }
  return a;
}

// 正常交易单全部关闭掉；去除近期成交单和设置成无止盈的手工单
void orderclosebyforex( int mySymPos,int mybuysellpoint,int buysellflag)
{
  int i,SymPos,NowMagicNumber,ticket;
  string my_symbol;
  double vbid,vask;

  int buysellpoint;
  int subbuysellpoint;

  for (i = 0; i < OrdersTotal(); i++)
  {
    if (OrderSelect(i,SELECT_BY_POS,MODE_TRADES))
    {
      if(isvalidmagicnumberall((int)OrderMagicNumber()) == true)
      {     

        SymPos = ((int)OrderMagicNumber()) /MAINMAGIC-1;
        NowMagicNumber = OrderMagicNumber() - (SymPos+1) *MAINMAGIC;

        buysellpoint = ((int)NowMagicNumber) /100;        
        subbuysellpoint = (NowMagicNumber%100);   
          
        my_symbol = MySymbol[SymPos];
        
        vbid    = MarketInfo(my_symbol,MODE_BID);             
        vask    = MarketInfo(my_symbol,MODE_ASK); 

        if(((buysellpoint>0)&&(buysellpoint<=3*HBUYSELLALGNUM))&&(mySymPos == SymPos)&&(mybuysellpoint ==buysellpoint))
        {
          //当去掉止盈的时候，程序对该单放弃监控，转为手动监控，通常是指那些基本面同步发生了重大同方向的变化，且适合长期持有的单子；改为手工持单
          //一般情况下不触发
         // if(OrderTakeProfit()>0.01)
          {
           // if((TimeCurrent()-OrderOpenTime())>BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].keepperiod)
            {
              if((buysellflag==1)&&(OrderType()==OP_BUY))
              {
                ticket =OrderClose(OrderTicket(),OrderLots(),vbid,20,Red);
                  
                 if(ticket <0)
                 {
                  Print(BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].MagicName+"orderclosebyforex buy ordercloseall with vbid failed with error #",GetLastError());
                 }
                 else
                 {     
                  BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].pendingstate = HPENDINGSTATEEMPTY;                
                  Print(BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].MagicName+"orderclosebyforex buy ordercloseall with vbid  successfully");
                 }      
                Sleep(1000); 
            
              }
              
              if((buysellflag==-1)&&(OrderType()==OP_SELL))
              {
                ticket =OrderClose(OrderTicket(),OrderLots(),vask,20,Red);
                  
                 if(ticket <0)
                 {
                  Print(BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].MagicName+"orderclosebyforex sell ordercloseall with vask  failed with error #",GetLastError());
                 }
                 else
                 {      
                  BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].pendingstate = HPENDINGSTATEEMPTY;                     
                  Print(BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].MagicName+"orderclosebyforex sell ordercloseall with vask   successfully");
                 }  
                Sleep(1000);         
            
              }

            }

          }   
        }   
      
      }
      
    }
  }
  
  return;
}


int getsymposbysymbol(string my_symbol)
{
  int mySymPos = -1;
  int SymPos;
  string my_symbol1;

  for(SymPos = 0; SymPos < symbolNum;SymPos++)
  {
    
    my_symbol1 =   MySymbol[SymPos];
    
    StringToUpper(my_symbol1);
    StringToUpper(my_symbol);
    
    if(0 <= StringFind(my_symbol1,my_symbol,0))
    {
      mySymPos = SymPos;
      break;
    }

   }

   return mySymPos;

}



/////////////////////////////////////////////////////////////////////////


void sendbuysellorder(int SymPos,int timeperiodnum,int buysellpoint,int subbuysellpoint,double bool_length,datetime timeexp)
{
  int my_timeperiod;
  string my_symbol;

  double vbid,vask; 
  double orderStopLevel;
  double orderpoint;
  double orderLots ;   
  double orderStopless ;
  double orderTakeProfit;
  double orderPrice;    
  double fakedTakeprofit,fakedstoploss;

  int ticket;
  int ttick;
  int vdigits ;


  my_symbol =   MySymbol[SymPos];
  my_timeperiod = timeperiod[timeperiodnum];  

  vask    = MarketInfo(my_symbol,MODE_ASK);
  vdigits = (int)MarketInfo(my_symbol,MODE_DIGITS);
  vbid    = MarketInfo(my_symbol,MODE_BID); 

  if(true == accountcheck())
  {


    ttick = 0;
    ticket = -1;
    while((ticket<0)&&(ttick<20))
    {
      //vask    = MarketInfo(my_symbol,MODE_ASK); 


      vask    = MarketInfo(my_symbol,MODE_ASK);
      vdigits = (int)MarketInfo(my_symbol,MODE_DIGITS);
      vbid    = MarketInfo(my_symbol,MODE_BID); 


      if(BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].buysellflag ==1)
      {

        orderPrice = vbid;  
        //日线boollength作为止损触发点，受浮动变化影响小，避免单个外汇对占比过高
        //bool_length = BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].AverageBoolLength;    
        orderStopless =orderPrice -   bool_length*BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoplossleverage;        
        orderTakeProfit = orderPrice + bool_length*BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].takeprofitleverage;
        BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoptailing = bool_length*BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoplossleverage*BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoptailtimes;

        /*参数修正*/ 
        orderStopLevel =MarketInfo(my_symbol,MODE_STOPLEVEL); 
        orderpoint = MarketInfo(my_symbol,MODE_POINT);
        orderStopLevel = 1.2*orderStopLevel;
         if ((orderPrice - orderStopless) < orderStopLevel*orderpoint)
         {
            orderStopless = orderPrice - orderStopLevel*orderpoint;
         }
         if ((orderTakeProfit - orderPrice) < orderStopLevel*orderpoint)
         {
            orderTakeProfit = orderPrice + orderStopLevel*orderpoint;
         }    
      }
      else
      {

        orderPrice = vask;  
        //日线boollength作为止损触发点，受浮动变化影响小，避免单个外汇对占比过高
        //bool_length = BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].AverageBoolLength;  

        orderStopless =orderPrice +   bool_length*BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoplossleverage;                
        orderTakeProfit = orderPrice - bool_length*BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].takeprofitleverage;
        BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoptailing = bool_length*BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoplossleverage*BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoptailtimes;

        /*参数修正*/ 
        orderStopLevel =MarketInfo(my_symbol,MODE_STOPLEVEL); 
        orderpoint = MarketInfo(my_symbol,MODE_POINT);
        orderStopLevel = 1.2*orderStopLevel;
         if ((orderStopless-orderPrice) < orderStopLevel*orderpoint)
         {
            orderStopless = orderPrice + orderStopLevel*orderpoint;
         }
         if ((orderPrice-orderTakeProfit) < orderStopLevel*orderpoint)
         {
            orderTakeProfit = orderPrice - orderStopLevel*orderpoint;
         }

      }

      fakedstoploss = orderStopless;
      fakedTakeprofit = orderTakeProfit;

      orderPrice = NormalizeDouble(orderPrice,vdigits);     
      orderStopless = NormalizeDouble(orderStopless,vdigits);     
      orderTakeProfit = NormalizeDouble(orderTakeProfit,vdigits);

      fakedstoploss = NormalizeDouble(fakedstoploss,vdigits);     
      fakedTakeprofit = NormalizeDouble(fakedTakeprofit,vdigits);

      BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].openprice = orderPrice;
      BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoploss = orderStopless;
      BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].takeprofit = orderTakeProfit;


      BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].fakedstoploss = fakedstoploss;
      BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].fakedtakeprofit = fakedTakeprofit;


      //根据资金量和承受能力动态调整交易手数，可有效控制回撤
      orderLots = autocalculateamount(SymPos,buysellpoint,subbuysellpoint);

      //外汇最小手数为0.01才允许交易作为交易的修正
      if((ForexIndex[SymPos].minlot>0.011)||(ForexIndex[SymPos].minlot<0.009))
      {
        return;
      }

      BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].orderlots = orderLots;  

      //挂单1个小时，尽量成交，直接成交不用设置挂单时间了
      //timeexp = TimeCurrent() + BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].fakedtimeexp*60;
          
                              
      Print(my_symbol+"BoolCrossRecord["+SymPos+"]["+buysellpoint+"][" +timeperiodnum+"]:"+ BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossFlag[0]+":" 
      + BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossFlag[1]+":"+ BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossFlag[2]+":"
      + BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossFlag[3]+":"+ BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossFlag[4]+":"
      + BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossFlag[5]+":"+ BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossFlag[6]+":"
      + BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossFlag[7]+":"+ BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossFlag[8]+":"
      + BoolCrossRecord[SymPos][buysellpoint][timeperiodnum].CrossFlag[9]);
                                            
      
      Print(BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].MagicName+"OrderSend:" + "orderLots=" + orderLots +"orderPrice ="
            +orderPrice+"orderStopless="+orderStopless+"orderTakeProfit="+orderTakeProfit); 
            
          

      //orderPrice = vask;          
      if(BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].buysellflag ==1)
      {


                  //orderclosebyforex(SymPos,-1);
                                    
                  //orderclosebyforex(SymPos,1);
        ticket = OrderSend(my_symbol,OP_BUY,
                 BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].orderlots,
                 vask,
                 5,
                     BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].fakedstoploss,
                 BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].fakedtakeprofit,
                 BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].MagicName,
                 BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].magicnumber,timeexp,Blue);           
      }
      else
      {

                  //orderclosebyforex(SymPos,1);
                  
        ticket = OrderSend(my_symbol,OP_SELL,
                 BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].orderlots,
                 vbid,
                 5,
                     BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].fakedstoploss,
                 BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].fakedtakeprofit,
                 BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].MagicName,
                 BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].magicnumber,timeexp,Blue);
      }
        
       if(ticket <0)
       {
        ttick++;
        Print("OrderSend Trend"+BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].MagicName+" failed with error #",GetLastError());           
        if(GetLastError()!=134)
        {
           //---- 3 seconds wait
           Sleep(3000);
           //---- refresh price data
           RefreshRates();            
        }
        else 
        {
          Print("Trend There is no enough money!");           
        }         
       }
       else
       {       
        ttick = 100;     
        TwentyS_Freq++;
        OneM_Freq++;
        ThirtyS_Freq++;
        FiveM_Freq++;
        ThirtyM_Freq++; 
        BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].opentime = TimeCurrent();

        //重复操作
        BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].openprice = orderPrice;
        BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoploss = orderStopless;
        BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].takeprofit = orderTakeProfit;
        BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].orderlots = orderLots;  

        //直接操作，非挂单状态
        BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].pendingstate = HPENDINGSTATEOPEN;               
        //记录当前一分钟的ibar位置
        BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].OneMOpenPos =   iBars(MySymbol[SymPos],timeperiod[0]);

        //BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoptailing = BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoptailtimes*BoolCrossRecord[SymPos][buysellpoint][0+4].AverageBoolLength;

        //BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoptailing = BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoptailtimes*(orderPrice-orderStopless)
        //                                  *BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].buysellflag;                                                              
        
        Print("OrderSend Trend"+BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].MagicName+"  successfully");
       }                          
      Sleep(1000);  
    }
    if((ttick>= 19) &&(ttick<25))
    {
        Print("!!Fatel error Trend sencouter please check your platform right now!");         
    }   
    
  }

}








// 每秒调用一次，反复执行的主体函数
//int start()

void OnTick(void)
{


  string mMailTitlle = "";

  int SymPos;
  double orderStopLevel;
  
  double orderLots ;   
  double orderStopless ;
  double orderTakeProfit;
  double orderPrice;

  string my_symbol;
  
  double MinValue3 = 100000;
  double MaxValue4=-1;
  ///////////
  int my_timeperiod = 0;
  int timeperiodnum = 0;
  ///////////
  

  
  //---
  // initial data checks
  // it is important to make sure that the expert works with a normal
  // chart and the user did not make any mistakes setting external 
  // variables (Lots, StopLoss, TakeProfit, 
  // TrailingStop) in our case, we check TakeProfit
  // on a chart of less than 100 bars
  //---

  if(iBars(NULL,0) <500)
  {
    Print("Bar Number less than 500");
    return;
  }


  orderStopLevel=0;
  orderLots = 0;   
  orderStopless = 0;
  orderTakeProfit = 0;
  orderPrice = 0;


  /*异常大量交易检测*/
  Freq_Count++;

  if(TwentyS_Freq > 9)
  {
     Print("detect ordersend unnormal");
     return;
  }
  else
  {
    if (0== (Freq_Count%20))
    {
       TwentyS_Freq = 0;
    }
  }

  if(ThirtyS_Freq > 15)
  {
      Print("detect ordersend unnorma2");
     return;
  }
  else
  {
    if (0== (Freq_Count%30))
    {
       ThirtyS_Freq = 0;
    }
  }

  if(OneM_Freq > 21)
  {
      Print("detect ordersend unnorma3");
     return;
  }
  else
  {
    if (0== (Freq_Count%60))
    {
       OneM_Freq = 0;
    }
  }

  if(FiveM_Freq > 37)
  {
      Print("detect ordersend unnorma4");
     return;
  }
  else
  {
    if (0== (Freq_Count%300))
    {
       FiveM_Freq = 0;
    }
  }

  if(ThirtyM_Freq > 55)
  {
      Print("detect ordersend unnorma5");
     return;
  }
  else
  {
    if (0== (Freq_Count%1800))
    {
       ThirtyM_Freq = 0;
    }
  }
  

  /*自动调整交易手数，即下午1-2点之间每隔5分钟检查一次设计*/
  autoadjustglobalamount();
  
  /*自动调整交易风险指数，即下午1-2点之间每隔5分钟检查一次设计*/
  autoadjustmaxlose();


  /*在交易时间段来临前确保使能全局交易标记，即下午1-2点之间每隔5分钟检查一次设计*/
  enableglobaltradeflag();

  
  /*所有货币对所有周期指标计算*/ 
  calculateindicator();
  
  //注册算法主体函数
  RegistAlgorithmMain();
   


  /////////////////////////////////////////////////
  PrintFlag = true;
  ChartEvent = iBars(NULL,0);     
  for(SymPos = 0; SymPos < symbolNum;SymPos++)
  { 

    for(timeperiodnum = 0; timeperiodnum < TimePeriodNum;timeperiodnum++)
    {
      my_symbol =   MySymbol[SymPos];
      my_timeperiod = timeperiod[timeperiodnum];    
      ForexTimePeriod[SymPos][timeperiodnum].ChartEvent = iBars(my_symbol,my_timeperiod);
    }
  }

  return;
   
   
}
//+------------------------------------------------------------------+



void RegistAlgorithmInit()
{
  //注册EURUSD的算法初始函数
  RegistInitEURUSDOneGood("EURUSD",(HBUYSELLALGNUM*0+1),(HBUYSELLALGNUM*0+1));
  RegistInitEURUSDTwo("EURUSD",(HBUYSELLALGNUM*0+2),(HBUYSELLALGNUM*0+2));  
}

void RegistAlgorithmMain()
{
   //注册EURUSD的算法主体函数
   RegistAlgorithmEURUSDOneGood("EURUSD",(HBUYSELLALGNUM*0+1),(HBUYSELLALGNUM*0+1));
   RegistAlgorithmEURUSDTwo("EURUSD",(HBUYSELLALGNUM*0+2),(HBUYSELLALGNUM*0+2));

}



void RegistInitEURUSDOneGood(string my_symbol,int buysellpoint,int algregistflag)
{

  int SymPos;
  int subbuysellpoint;
  //输入外汇参数有效性检测
  SymPos = getsymposbysymbol(my_symbol);
  if(SymPos < 0)
  {
    return;
  }

  Print("input symbol success:"+my_symbol);

  //特定算法初始化注册，如果已经注册了该函数
  if(BuySellPosRecord[SymPos][buysellpoint][0].algorithmflag == ALGORITHMFLAGDISABLE)
  {

     for(subbuysellpoint = 0; subbuysellpoint < HSUBBUYSELLPOINTNUM;subbuysellpoint++)
     {
       BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].algorithmflag = ALGORITHMFLAGENABLE;
       BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].algregistflag = algregistflag; 
     }
    Print("Regist Algorithm init  success:"+my_symbol);
  }
  else
  {
    return;    
  }


  //重新设置止盈和止损值
  for(subbuysellpoint = 0; subbuysellpoint < HSUBBUYSELLPOINTNUM;subbuysellpoint++)
  {

    //重新定义该算法的最大同时交易数
    BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].subbuysellnumber = 1;

    //设置止损和止盈的比例，这个是默认值，不同的算法需要重新进行设置，达到最好的效果
    BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoplossleverage = 60;
    BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].takeprofitleverage = 60; 
    //两倍的止损额度移动止损 
    BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoptailtimes = 2;    
    //移动止损和时间止盈设置生效
    BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].checkbuysellorderflag = ALGORITHMFLAGENABLE;
    
   //输入指标参数
   BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].iBool_Len = 2.0;
   BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].iBool_LenL = 1.8;
   BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].iBoll_B = 60;          
   BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].Move_Av = 2;  
   
    
  }


}

void RegistAlgorithmEURUSDOneGood(string my_symbol,int buysellpoint,int algregistflag)
{

  int my_timeperiod;
  int SymPos;
  int timeperiodnum;
  double vbid,vask; 
  double MinValue3 = 100000;
  double MaxValue4=-1;
  double orderStopLevel;
  double orderLots ;   
  double orderStopless ;
  double orderTakeProfit;
  double orderPrice;
  double bool_length;
  int subbuysellpoint;
  int NowMagicNumber,magicnumber; 
  int i;
  int ticket;
  double timekeep,decayvalue;
  double value0,value1,value2,value3;

  double boll_up_B,boll_low_B,boll_mid_B; 

  int    vdigits ;

  
  int oldSymPos,oldbuysellpoint,oldsubbuysellpoint;
 
  orderStopLevel=0;
  orderLots = 0;   
  orderStopless = 0;
  orderTakeProfit = 0;
  orderPrice = 0;
  timeperiodnum = 0;  
  my_timeperiod = timeperiod[timeperiodnum]; 


  //输入外汇参数有效性检测
  SymPos = getsymposbysymbol(my_symbol);
  if(SymPos < 0)
  {
    return;
  }


  //该算法是否进行了初始化注册
  if(BuySellPosRecord[SymPos][buysellpoint][0].algorithmflag == ALGORITHMFLAGDISABLE)
  {
    return;
  }

  //该算法没有进行算法初始化的时候，会退出
  if(BuySellPosRecord[SymPos][buysellpoint][0].algregistflag != algregistflag)
  {
    return;
  }



  //确立主时间周期，此时为1分钟时间周期
  timeperiodnum = BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].timeperiodnum; 
  my_symbol =   MySymbol[SymPos];
  my_timeperiod = timeperiod[timeperiodnum];  
  

  //确保寻找买卖点是每个主时间周期计算一次，而不是每个tick计算一次，进入开仓算法程序
  if ( ForexTimePeriod[SymPos][timeperiodnum].ChartEvent != iBars(my_symbol,my_timeperiod))
  {
  
    //Print("Regist Algorithm Main Enter Open Order ALG :"+my_symbol);   
     vask    = MarketInfo(my_symbol,MODE_ASK);
     vbid    = MarketInfo(my_symbol,MODE_BID); 
     
    if(
      
      (BoolCrossRecord[SymPos][buysellpoint][timeperiodnum+2].StrongWeak>0.8)

                              
      &&(opendaycheck(SymPos) == true)
      &&(tradetimecheck(SymPos) ==true)       
      )
    {
      
         {


           //当前买卖点没有交易产生
             subbuysellpoint = getfreesubbuysellnumber(SymPos,buysellpoint);        
             if(subbuysellpoint >= 0)
             {

               bool_length = BoolCrossRecord[SymPos][buysellpoint][timeperiodnum+2].AverageBoolLength;
               sendbuysellorder(SymPos,timeperiodnum,buysellpoint,subbuysellpoint,bool_length,0);        
             }   


     
         }
     
    }




  }



  timeperiodnum = 0;  
  my_timeperiod = timeperiod[timeperiodnum]; 
  //确保寻找买卖点是每个主时间周期计算一次，而不是每个tick计算一次，进入平仓算法程序
  //部分算法的平仓是依赖于止损和止盈的，这种情况下，本分支就为空
  if ( ForexTimePeriod[SymPos][timeperiodnum].ChartEvent != iBars(my_symbol,my_timeperiod))
  {  

  
     
     for (i = 0; i < OrdersTotal(); i++)
     {
         if (OrderSelect(i,SELECT_BY_POS,MODE_TRADES))
         {
       
         magicnumber = OrderMagicNumber();
         oldSymPos = ((int)magicnumber) /MAINMAGIC-1;
         NowMagicNumber = magicnumber - (oldSymPos+1) *MAINMAGIC;
       
         if((oldSymPos>=0)&&(oldSymPos<symbolNum))
         {
           my_symbol = MySymbol[oldSymPos];
   
           oldsubbuysellpoint = (NowMagicNumber%100);  
           if((oldsubbuysellpoint>= 0)&&(oldsubbuysellpoint<HSUBBUYSELLPOINTNUM))
           {
             oldbuysellpoint = ((int)NowMagicNumber) /100;
             if((oldbuysellpoint>=0)&&(oldbuysellpoint<HBUYSELLALGNUM*3))
             {
   
               vbid    = MarketInfo(my_symbol,MODE_BID);   
               vask    = MarketInfo(my_symbol,MODE_ASK);         
               timeperiodnum = 0;  
               my_timeperiod = timeperiod[timeperiodnum];  
   
   
   
               //当去掉止盈的时候，程序对该单放弃监控，转为手动监控，通常是指那些基本面同步发生了重大同方向的变化，且适合长期持有的单子；改为手工持单
               //一般情况下不触发
               if((OrderTakeProfit()>0.01)&&(oldSymPos==SymPos)&&(buysellpoint == oldbuysellpoint))
               {
   
                 //实际止盈和止损探测，每时每刻都要探测
                 vbid    = MarketInfo(my_symbol,MODE_BID);   
                 vask    = MarketInfo(my_symbol,MODE_ASK); 
   

                 //买单，根据实际止损止盈值确定是否发送止损和止盈的指令
                 if(OrderType()==OP_BUY)
                 {
   
                    if(BoolCrossRecord[SymPos][buysellpoint][BuySellPosRecord[SymPos][buysellpoint][oldsubbuysellpoint].timeperiodnum+2].StrongWeak<0.2)
                    {
                        ticket =OrderClose(OrderTicket(),OrderLots(),vbid,20,Red);
                          
                         if(ticket <0)
                         {
                          Print("Buy Order Closed caltakeprofit failed with error #",GetLastError());
                         }
                         else
                         {      
      
                          BuySellPosRecord[SymPos][buysellpoint][oldsubbuysellpoint].pendingstate = HPENDINGSTATEEMPTY;  
                          Print("Buy Order Closed buytrade  successfully ");
                         }                                                                          
                      
                         Sleep(1000);                      
                       
                    
                    
                    
                    }
      
            
   
                 }
                 else
                 {
                   ;
                 }
   
   
                 //恢复原状
                 timeperiodnum = 0;  
                 my_timeperiod = timeperiod[timeperiodnum];  
   
               }
   
             } 
           } 
         }     
       
       }       
     }  
  
    //Print("Regist Algorithm Main Enter Close Order ALG :"+my_symbol);  
  }


}





void RegistInitEURUSDTwo(string my_symbol,int buysellpoint,int algregistflag)
{

  int SymPos;
  int subbuysellpoint;
  //输入外汇参数有效性检测
  SymPos = getsymposbysymbol(my_symbol);
  if(SymPos < 0)
  {
    return;
  }

  Print("input RegistInitEURUSDTwo symbol success:"+my_symbol);

  //特定算法初始化注册，如果已经注册了该函数
  if(BuySellPosRecord[SymPos][buysellpoint][0].algorithmflag == ALGORITHMFLAGDISABLE)
  {

     for(subbuysellpoint = 0; subbuysellpoint < HSUBBUYSELLPOINTNUM;subbuysellpoint++)
     {
       BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].algorithmflag = ALGORITHMFLAGENABLE;
       BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].algregistflag = algregistflag; 
     }
    Print("Regist Algorithm init  success:"+my_symbol);
  }
  else
  {
    return;    
  }


  //重新设置止盈和止损值
  for(subbuysellpoint = 0; subbuysellpoint < HSUBBUYSELLPOINTNUM;subbuysellpoint++)
  {

    //重新定义该算法的最大同时交易数
    BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].subbuysellnumber = 1;

    //设置止损和止盈的比例，这个是默认值，不同的算法需要重新进行设置，达到最好的效果
    BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoplossleverage = 60;
    BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].takeprofitleverage = 60; 
    //两倍的止损额度移动止损 
    BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].stoptailtimes = 2;    
    //移动止损和时间止盈设置生效
    BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].checkbuysellorderflag = ALGORITHMFLAGENABLE;
    
   //输入指标参数
   BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].iBool_Len = 2.0;
   BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].iBool_LenL = 1.8;
   BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].iBoll_B = 60;          
   BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].Move_Av = 2;  
   
    
  }

}




void RegistAlgorithmEURUSDTwo(string my_symbol,int buysellpoint,int algregistflag)
{

  int my_timeperiod;
  int SymPos;
  int timeperiodnum;
  double vbid,vask; 
  double MinValue3 = 100000;
  double MaxValue4=-1;
  double orderStopLevel;
  double orderLots ;   
  double orderStopless ;
  double orderTakeProfit;
  double orderPrice;
  double bool_length;
  int subbuysellpoint;
  int NowMagicNumber,magicnumber; 
  int i;
  int ticket;
  double timekeep,decayvalue;
  double value0,value1,value2,value3;

  double boll_up_B,boll_low_B,boll_mid_B; 

  int    vdigits ;

  
  int oldSymPos,oldbuysellpoint,oldsubbuysellpoint;
 
  orderStopLevel=0;
  orderLots = 0;   
  orderStopless = 0;
  orderTakeProfit = 0;
  orderPrice = 0;
  timeperiodnum = 0;  
  my_timeperiod = timeperiod[timeperiodnum]; 


  //输入外汇参数有效性检测
  SymPos = getsymposbysymbol(my_symbol);
  if(SymPos < 0)
  {
    return;
  }


  //该算法是否进行了初始化注册
  if(BuySellPosRecord[SymPos][buysellpoint][0].algorithmflag == ALGORITHMFLAGDISABLE)
  {
    return;
  }

  //该算法没有进行算法初始化的时候，会退出
  if(BuySellPosRecord[SymPos][buysellpoint][0].algregistflag != algregistflag)
  {
    return;
  }



  //确立主时间周期，此时为1分钟时间周期
  timeperiodnum = BuySellPosRecord[SymPos][buysellpoint][subbuysellpoint].timeperiodnum; 
  my_symbol =   MySymbol[SymPos];
  my_timeperiod = timeperiod[timeperiodnum];  
  

  //确保寻找买卖点是每个主时间周期计算一次，而不是每个tick计算一次，进入开仓算法程序
  if ( ForexTimePeriod[SymPos][timeperiodnum].ChartEvent != iBars(my_symbol,my_timeperiod))
  {
  
    //Print("Regist Algorithm Main Enter Open Order ALG :"+my_symbol);   
     vask    = MarketInfo(my_symbol,MODE_ASK);
     vbid    = MarketInfo(my_symbol,MODE_BID); 
     
    if(
      
      (BoolCrossRecord[SymPos][buysellpoint][timeperiodnum+2].StrongWeak<0.2)

                              
      &&(opendaycheck(SymPos) == true)
      &&(tradetimecheck(SymPos) ==true)       
      )
    {
      
         {


           //当前买卖点没有交易产生
             subbuysellpoint = getfreesubbuysellnumber(SymPos,buysellpoint);        
             if(subbuysellpoint >= 0)
             {

               bool_length = BoolCrossRecord[SymPos][buysellpoint][timeperiodnum+2].AverageBoolLength;
               sendbuysellorder(SymPos,timeperiodnum,buysellpoint,subbuysellpoint,bool_length,0);        
             }   


     
         }
     
    }




  }



  timeperiodnum = 0;  
  my_timeperiod = timeperiod[timeperiodnum]; 
  //确保寻找买卖点是每个主时间周期计算一次，而不是每个tick计算一次，进入平仓算法程序
  //部分算法的平仓是依赖于止损和止盈的，这种情况下，本分支就为空
  if ( ForexTimePeriod[SymPos][timeperiodnum].ChartEvent != iBars(my_symbol,my_timeperiod))
  {  

  
     
     for (i = 0; i < OrdersTotal(); i++)
     {
         if (OrderSelect(i,SELECT_BY_POS,MODE_TRADES))
         {
       
         magicnumber = OrderMagicNumber();
         oldSymPos = ((int)magicnumber) /MAINMAGIC-1;
         NowMagicNumber = magicnumber - (oldSymPos+1) *MAINMAGIC;
       
         if((oldSymPos>=0)&&(oldSymPos<symbolNum))
         {
           my_symbol = MySymbol[oldSymPos];
   
           oldsubbuysellpoint = (NowMagicNumber%100);  
           if((oldsubbuysellpoint>= 0)&&(oldsubbuysellpoint<HSUBBUYSELLPOINTNUM))
           {
             oldbuysellpoint = ((int)NowMagicNumber) /100;
             if((oldbuysellpoint>=0)&&(oldbuysellpoint<HBUYSELLALGNUM*3))
             {
   
               vbid    = MarketInfo(my_symbol,MODE_BID);   
               vask    = MarketInfo(my_symbol,MODE_ASK);         
               timeperiodnum = 0;  
               my_timeperiod = timeperiod[timeperiodnum];  
   
   
   
               //当去掉止盈的时候，程序对该单放弃监控，转为手动监控，通常是指那些基本面同步发生了重大同方向的变化，且适合长期持有的单子；改为手工持单
               //一般情况下不触发
               if((OrderTakeProfit()>0.01)&&(oldSymPos==SymPos)&&(buysellpoint == oldbuysellpoint))
               {
   
                 //实际止盈和止损探测，每时每刻都要探测
                 vbid    = MarketInfo(my_symbol,MODE_BID);   
                 vask    = MarketInfo(my_symbol,MODE_ASK); 
   

                 //买单，根据实际止损止盈值确定是否发送止损和止盈的指令
                 if(OrderType()==OP_SELL)
                 {
   
                    if(BoolCrossRecord[SymPos][buysellpoint][BuySellPosRecord[SymPos][buysellpoint][oldsubbuysellpoint].timeperiodnum+2].StrongWeak>0.8)
                    {
                        ticket =OrderClose(OrderTicket(),OrderLots(),vask,20,Red);
                          
                         if(ticket <0)
                         {
                          Print("Buy Order Closed caltakeprofit failed with error #",GetLastError());
                         }
                         else
                         {      
      
                          BuySellPosRecord[SymPos][buysellpoint][oldsubbuysellpoint].pendingstate = HPENDINGSTATEEMPTY;  
                          Print("Buy Order Closed buytrade  successfully ");
                         }                                                                          
                      
                         Sleep(1000);                      
                       
                    
                    
                    
                    }
      
            
   
                 }
                 else
                 {
                   ;
                 }
   
   
                 //恢复原状
                 timeperiodnum = 0;  
                 my_timeperiod = timeperiod[timeperiodnum];  
   
               }
   
             } 
           } 
         }     
       
       }       
     }  
  
    //Print("Regist Algorithm Main Enter Close Order ALG :"+my_symbol);  
  }


}








