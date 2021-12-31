/*******************************************************************************
 * Copyright (c) quickfixengine.org  All rights reserved.
 *
 * This file is part of the QuickFIX FIX Engine
 *
 * This file may be distributed under the terms of the quickfixengine.org
 * license as defined by quickfixengine.org and appearing in the file
 * LICENSE included in the packaging of this file.
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING
 * THE WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 * See http://www.quickfixengine.org/LICENSE for licensing information.
 *
 * Contact ask@quickfixengine.org if any conditions of this licensing
 * are not clear to you.
 ******************************************************************************/

package quickfix.examples.banzai;

import com.pactera.fix.custom.ExecutionStyle;
import com.pactera.fix.custom.OneClickAction;
import com.pactera.fix.custom.OneClickTolerance;
import com.pactera.fix.custom.StreamingQuote;
import com.pactera.fix.domain.NOS;
import com.pactera.fix.domain.QR;
import org.quickfixj.jmx.JmxExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import quickfix.field.*;
import quickfix.fix43.NewOrderSingle;
import quickfix.fix50sp1.QuoteRequest;

import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * Entry point for the Downstream application.
 */
public class Downstream {
    private static final CountDownLatch shutdownLatch = new CountDownLatch(1);

    private static final Logger log = LoggerFactory.getLogger(Downstream.class);
    private static Downstream downstream;
    private boolean initiatorStarted = false;
    private static Initiator initiator = null;

    public Downstream(String[] args) throws Exception {
        InputStream inputStream = null;
        if (args.length == 0) {
            inputStream = Downstream.class.getResourceAsStream("banzai.cfg");
        } else if (args.length == 1) {
            inputStream = new FileInputStream(args[0]);
        }
        if (inputStream == null) {
            System.out.println("usage: " + Downstream.class.getName() + " [configFile].");
            return;
        }
        SessionSettings settings = new SessionSettings(inputStream);
        inputStream.close();

        boolean logHeartbeats = Boolean.valueOf(System.getProperty("logHeartbeats", "true"));

        OrderTableModel orderTableModel = new OrderTableModel();
        ExecutionTableModel executionTableModel = new ExecutionTableModel();
        BanzaiApplication application = new BanzaiApplication(orderTableModel, executionTableModel);
        MessageStoreFactory messageStoreFactory = new FileStoreFactory(settings);
        LogFactory logFactory = new ScreenLogFactory(true, true, true, logHeartbeats);
        MessageFactory messageFactory = new DefaultMessageFactory();

        initiator = new SocketInitiator(application, messageStoreFactory, settings, logFactory, messageFactory);

        JmxExporter exporter = new JmxExporter();
        exporter.register(initiator);
    }

    public synchronized void logon() {
        if (!initiatorStarted) {
            try {
                initiator.start();
                initiatorStarted = true;
            } catch (Exception e) {
                log.error("Logon failed", e);
            }
        } else {
            for (SessionID sessionId : initiator.getSessions()) {
                Session.lookupSession(sessionId).logon();
            }
        }
    }

    public void logout() {
        for (SessionID sessionId : initiator.getSessions()) {
            Session.lookupSession(sessionId).logout("user requested");
        }
    }

    public void stop() {
        shutdownLatch.countDown();
    }

    public static void main(String[] args) throws Exception {
        try {
            downstream = new Downstream(new String[]{});
            downstream.logon();
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }finally{
            String symbols="AUDCAD,AUDCHF,AUDHKD,AUDJPY,AUDNZD,AUDUSD,CADCHF,CADHKD,CADJPY,CHFHKD,CHFJPY,EURAUD,EURCAD,EURCHF,EURGBP,EURHKD,EURJPY,EURNZD,EURUSD,GBPAUD,GBPCAD,GBPCHF,GBPHKD,GBPJPY,GBPNZD,GBPUSD,HKDCNH,HKDJPY,NZDCAD,NZDCHF,NZDHKD,NZDJPY,NZDUSD,USDCAD,USDCHF,USDCNH,USDHKD,USDJPY,XAUUSD";
            if(args!=null&&args.length>0){
                int tradeType=Integer.valueOf(args[0]);
                if(tradeType==3){
                    String settlType=args[1];
                    String amount=args[2];
                    char way=args[3].charAt(0);
                    String orderId=args[4];
                    String userId=args[5];
                    String clientId=args[6];
                    String symbol=args[7];
                    int DPS=Integer.valueOf(args[8]);
                    Double streamQuote=Double.valueOf(args[9]);
                    int oneClickAction=Integer.valueOf(args[10]);
                    NOS nos=new NOS();
                    nos.setPartyID("EFX_TRADE");
                    nos.setQuoteReqID("a");
                    nos.setQuoteID("a");
                    nos.setClOrdID(UUID.randomUUID().toString());
                    nos.setSide(way);
                    nos.setAccount(clientId);
                    nos.setIssuer(userId);
                    nos.setQuoteRespID(orderId);
                    nos.setQuoteMsgID("GenIdeal");
                    nos.setSpread(Double.valueOf(10));
                    nos.setTradeDate(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    nos.setSettlType(settlType);
                    nos.setExecutionStyle(3);
                    nos.setPrice(Double.valueOf("0.0"));
                    nos.setSymbol(symbol);
                    nos.setOrderQty(Double.valueOf(amount));
                    nos.setDps(DPS);
                    nos.setOneClickTolerance(Double.valueOf("0.0007"));
                    nos.setOneClickAction(oneClickAction);
                    nos.setStreamingQuote(Double.valueOf(streamQuote));
                    testNewOrderSingle(nos);
                }else{
                    String settlType=args[1];
                    String amount=args[2];
                    char way=args[3].charAt(0);
                    String symbol=args[4];
                    QR qr=new QR();
                    qr.setQuoteReqID(UUID.randomUUID().toString());
                    qr.setPartyID("EFX_TRADE");
                    qr.setSymbol(symbol);
                    qr.setSide(way);
                    qr.setExecutionStyle(1);
                    qr.setSettlType(settlType);
                    qr.setAccount("");
                    qr.setOrderQty(Double.valueOf(amount));
                    qr.setTransactTime(LocalDateTime.now());
                    testQuoteRequest(qr);
                }
            }else{
                localNOS();
//                localQR();
//                testQuoteCancel();
                shutdownLatch.countDown();
            }

//            }
        }
        shutdownLatch.await();
    }

    private static void localQR() throws SessionNotFound {
        String clientMapping="EFXTraderPrice.EFXTraderPriceSIT,MarginPromotion.MarginPromotionSIT,MarginVIP.MarginVIPSIT,MarginPlatinum.MarginPlatinumSIT,MarginGold.MarginGoldSIT,MarginSilver.MarginSilverSIT,MarginWide.MarginWideSIT,MarginEvent.MarginEventSIT,NonMarginPromotion.NonMarginPromotionSIT,NonMarginVIP.NonMarginVIPSIT,NonMarginPlatinum.NonMarginPlatinumSIT,NonMarginGold.NonMarginGoldSIT,NonMarginSilver.NonMarginSilverSIT,NonMarginWide.NonMarginWideSIT,NonMarginEvent.NonMarginEventSIT";
        String[] items = clientMapping.split("[,]");
        for(int i = 0; i< 1; i++){
            if(items[6]!=null&&!items[6].equals("")){
                QR qr=new QR();
                qr.setQuoteReqID(UUID.randomUUID().toString());
                qr.setPartyID("EFX_TRADE");
                qr.setSymbol("USD.JPY");
                qr.setSide('7');
                qr.setExecutionStyle(2);
                qr.setSettlType("0");
                qr.setAccount(items[6].split("[.]")[0]);
                qr.setOrderQty(Double.valueOf("5000"));
                qr.setTransactTime(LocalDateTime.now());
                testQuoteRequest(qr);
            }
        }
    }

    private static void localNOS() throws SessionNotFound {
        String clientMapping="EFXTraderPrice.EFXTraderPriceSIT,MarginPromotion.MarginPromotionSIT,MarginVIP.MarginVIPSIT,MarginPlatinum.MarginPlatinumSIT,MarginGold.MarginGoldSIT,MarginSilver.MarginSilverSIT,MarginWide.MarginWideSIT,MarginEvent.MarginEventSIT,NonMarginPromotion.NonMarginPromotionSIT,NonMarginVIP.NonMarginVIPSIT,NonMarginPlatinum.NonMarginPlatinumSIT,NonMarginGold.NonMarginGoldSIT,NonMarginSilver.NonMarginSilverSIT,NonMarginWide.NonMarginWideSIT,NonMarginEvent.NonMarginEventSIT";
//        for(String client:clientMapping.split("[,]")){
//            if(client!=null&&!client.equals("")){
                NOS nos=new NOS();
                nos.setPartyID("EFX_TRADE");
                nos.setQuoteReqID("a");
                nos.setQuoteID("a");
                nos.setClOrdID(UUID.randomUUID().toString());
                nos.setSide('1');
                nos.setAccount(""/**client.split("[.]")[0]*/);
                nos.setIssuer("1004");
                nos.setQuoteRespID("23631");
                nos.setQuoteMsgID("GenIdeal");
                nos.setSpread(Double.valueOf(10));
                nos.setTradeDate(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                nos.setSettlType("0");
                nos.setExecutionStyle(3);
                nos.setPrice(Double.valueOf("0.0"));
                nos.setSymbol("USD.JPY");
                nos.setOrderQty(Double.valueOf("2500"));
                nos.setOneClickTolerance(Double.valueOf("0.007"));
                nos.setOneClickAction(2);
                nos.setStreamingQuote(Double.valueOf("113.445"));
                testNewOrderSingle(nos);
//            }
//        }
    }

    private static void testNewOrderSingle(NOS nos) throws SessionNotFound {
        NewOrderSingle newOrderSingle = new NewOrderSingle();
        newOrderSingle.setField(new PartyID(nos.getPartyID()));
        newOrderSingle.setField(new QuoteReqID(nos.getQuoteReqID()));
        newOrderSingle.setField(new QuoteID(nos.getQuoteID()));
        newOrderSingle.setField(new ClOrdID(nos.getClOrdID()));
        newOrderSingle.setField(new Side(nos.getSide()));//1-b,2-s
        newOrderSingle.setField(new Account(nos.getAccount()));//"client1@trapi"
        newOrderSingle.setField(new Issuer(nos.getIssuer()));
        newOrderSingle.setField(new QuoteRespID(nos.getQuoteRespID()));
        newOrderSingle.setField(new QuoteMsgID(nos.getQuoteMsgID()));
        newOrderSingle.setField(new Spread(nos.getSpread()));//markup
        newOrderSingle.setField(new TradeDate(nos.getTradeDate()));
        //added=====================================
        newOrderSingle.setField(new SettlType(nos.getSettlType()));//0-SPOT,1-TODAY
        newOrderSingle.setField(new ExecutionStyle(nos.getExecutionStyle()));//1-rfq,2-rfs,3-one click
        newOrderSingle.setField(new Price(nos.getPrice()));
        //one click fixed==================================
        newOrderSingle.setField(new Symbol(nos.getSymbol()));
        newOrderSingle.setField(new OrderQty(nos.getOrderQty()));
        newOrderSingle.setField(new OneClickTolerance(nos.getOneClickTolerance()));
        newOrderSingle.setField(new OneClickAction(nos.getOneClickAction()));//1-FILL_AT_MY_RATE_ONLY,2-FILL_AT_LATEST,3-SLIPPAGE
        newOrderSingle.setField(new StreamingQuote(nos.getStreamingQuote()));

        Session.sendToTarget(newOrderSingle,initiator.getSessions().get(0));
    }

    private static void testQuoteRequest(QR quoteObj) throws SessionNotFound{
        QuoteRequest qr=new QuoteRequest();
        qr.setField(new QuoteReqID(quoteObj.getQuoteReqID()));
        qr.setField(new PartyID(quoteObj.getPartyID()));
        qr.setField(new Symbol(quoteObj.getSymbol()));
        qr.setField(new Side(quoteObj.getSide()));//1-b,2-s,7-not tell
        qr.setField(new ExecutionStyle(quoteObj.getExecutionStyle()));//1.rfq,2.rfs
        qr.setField(new SettlType(quoteObj.getSettlType()));//0-SPOT,1-TODAY
        qr.setField(new Account(quoteObj.getAccount()));//0-SPOT,1-2D
        qr.setField(new OrderQty(quoteObj.getOrderQty()));
        qr.setField(new TransactTime(new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()));
        Session.sendToTarget(qr,initiator.getSessions().get(0));
    }

}
