from FinamPy.FinamPy import FinamPy
from FinamPy.proto.tradeapi.v1.candles_pb2 import DayCandleTimeFrame, DayCandleInterval, IntradayCandleTimeFrame, \
    IntradayCandleInterval
from datetime import datetime, timedelta
from time import time
from google.protobuf.timestamp_pb2 import Timestamp


class Config:
    ClientIds = ('523341R6J68',)  # Торговые счёта
    AccessToken = 'CAEQzM/JARoYxQxmFIu700MizzPUi1920c8EoNhN79ok'


fp_provider = FinamPy(Config.AccessToken)  # Подключаемся
client_id = Config.ClientIds[0]


def last_price(ticker, board):
    next_utc_bar_date = datetime.now()
    interval = IntradayCandleInterval(count=1)
    to_ = getattr(interval, 'to')
    date_to = Timestamp(seconds=int(next_utc_bar_date.timestamp()), nanos=next_utc_bar_date.microsecond * 1_000)
    to_.seconds = date_to.seconds
    to_.nanos = date_to.nanos
    res = (fp_provider.get_intraday_candles(security_board=board, security_code=ticker,
                                            time_frame=IntradayCandleTimeFrame.INTRADAYCANDLE_TIMEFRAME_M1,
                                            interval=interval))
    num = res.candles[0].close.num
    scale = res.candles[0].close.scale
    price = num / (10 ** scale)
    return price


def open_price(ticker, board):
    utc_bar_date = datetime.now()
    interval = DayCandleInterval(count=1)
    to_ = getattr(interval, 'to')
    to_.year = utc_bar_date.year
    to_.month = utc_bar_date.month
    to_.day = utc_bar_date.day
    res = (fp_provider.get_day_candles(security_board=board, security_code=ticker,
                                       time_frame=DayCandleTimeFrame.DAYCANDLE_TIMEFRAME_D1, interval=interval))
    num = res.candles[0].open.num
    scale = res.candles[0].open.scale
    price = num / (10 ** scale)
    return price


def candles_array(ticker, board):
    utc_bar_date = datetime.now()
    interval = DayCandleInterval(count=30)
    to_ = getattr(interval, 'to')
    to_.year = utc_bar_date.year
    to_.month = utc_bar_date.month
    to_.day = utc_bar_date.day
    res = (fp_provider.get_day_candles(security_board=board, security_code=ticker,
                                       time_frame=DayCandleTimeFrame.DAYCANDLE_TIMEFRAME_D1, interval=interval))
    candles = []
    for i in range(30):
        openn = res.candles[i].open.num
        opens = res.candles[i].open.scale
        open = openn / (10 ** opens)
        closen = res.candles[i].close.num
        closes = res.candles[i].close.scale
        close = closen / (10 ** closes)
        lown = res.candles[i].low.num
        lows = res.candles[i].low.scale
        low = lown / (10 ** lows)
        highn = res.candles[i].high.num
        highs = res.candles[i].high.scale
        high = highn / (10 ** highs)
        candle = [high, low, open, close]
        candles.append(candle)
    return candles
