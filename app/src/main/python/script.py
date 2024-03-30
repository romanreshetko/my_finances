import time

from FinamPy.FinamPy import FinamPy
from decimal import Decimal


class Config:
    ClientIds = ('523341R6J68',)  # Торговые счёта
    AccessToken = 'CAEQzM/JARoYxQxmFIu700MizzPUi1920c8EoNhN79ok'


_price = 0


def get_current_price(order_book):
    """Получаем информацию по ближайшей текущей цене тикера - из стакана"""
    global _price  # Обработчик события прихода подписки на стакан
    _price = order_book.asks[0].price
    return _price


fp_provider = FinamPy(Config.AccessToken)  # Подключаемся
client_id = Config.ClientIds[0]


def get_price(ticker, board):
    price = 0
    global _price
    _price = 0
    fp_provider.on_order_book = get_current_price
    fp_provider.subscribe_order_book(ticker, board, 'orderbook1')  # Подписываемся на стакан тикера
    while price == 0:
        price = _price
    fp_provider.unsubscribe_order_book('orderbook1', ticker, board)  # Отписываемся от стакана тикера
    return price


