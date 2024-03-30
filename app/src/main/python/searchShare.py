from FinamPy.FinamPy import FinamPy


class Config:
    ClientIds = ('523341R6J68',)  # Торговые счёта
    AccessToken = 'CAEQzM/JARoYxQxmFIu700MizzPUi1920c8EoNhN79ok'


fp_provider = FinamPy(Config.AccessToken)  # Подключаемся
client_id = Config.ClientIds[0]


def search_share(ticker):
    with open("securities.txt", "r") as file:
        lines = file.readlines()
        if ticker + '\n' in lines:
            return "TQBR"
        else:
            return None
