# Introdução #

O NetBaker é um framework para criar servidores TCP para a plataforma Android. Você poderá criar aplicações com servidores TCP de maneira fácil usando estas classes.

# Detalhes #

O NetBaker vem com um exemplo de servidor HTTP bem simples, o qual você pode modificar para usar. Você pode também criar seu próprio servidor, implementando a interface InetBakerProtocol.

# Instalação #

Por favor LEIA o arquivo de licença "licencaDeUso.txt", dentro do projeto.

  1. Copie o pacote "org.thecodebakers.webxpose.netbaker.core" para a pasta de fontes do seu projeto;
  1. Crie sua própria implementação da interface "InetBakerProtocol". Você deve implementar ambos os métodos. Veja os comentários dentro do código de exemplo para maiores instruções;
  1. Adicione as permissões de rede no arquivo "AndroidManifest.xml", como mostrado no exemplo;
  1. Copie os arquivos de string "netbaker\_core\_messages.xml" e "netbaker\_starter\_messages.xml" para a pasta "res/values" do seu projeto;
  1. Use a classe "NetBakerStarter" para verificar e iniciar o seu servidor;

Exemplo básico de como iniciar um servidor baseado no NetBaker:
```
    	NetBakerStarter.context = this.getApplicationContext();
    	NetBakerStarter.debug = true;
    	NetBakerStarter.toastError = true;
    	if (NetBakerStarter.checkNetwork().size() > 0) {
    		if (!NetBakerStarter.checkAdminPort(8081)) {
    			NetBakerStarter.startService(8080, 8081, "Test", "org.thecodebakers.webxpose.netbaker.protocols.NetBakerHttp");
    		}
    	}
```

Onde:
  * 8080 - Porta TCP principal. Deve ser superior a 1024;
  * 8081 - Porta TCP administrativa. Deve ser superior a 1024 e diferente da porta principal;
  * "Test" - Nome do servidor;
  * "org.thecodebakers.webxpose.netbaker.protocols.NetBakerHttp" - nossa implementação de um servidor HTTP. Você deve criar sua própria classe;

Esta é a maneira básica de parar um servidor baseado no NetBaker:
```
    	NetBakerStarter.context = this.getApplicationContext();
    	NetBakerStarter.debug = true;
    	NetBakerStarter.toastError = true;
    	if (NetBakerStarter.checkNetwork().size() > 0) {
    		if (NetBakerStarter.checkAdminPort(8081)) {
    			NetBakerStarter.requestStop(8081);
    		}
    	}
```

Os únicos componentes que você precisa criar são:
  * A Activity de sua aplicação, a qual invoca os métodos da classe "NetBakerStarter";
  * A sua implementação da interface "InetBakerProtocol";
  * Por favor, use código de situação de mensagens superior a 100;

O NetBaker utiliza alguns textos padronizados, os quais podem ser traduzidos, caso você queira. Para ser consistente, eu criei um código de situação, que é relacionado à situação e ao texto da mensagem. Se o NetBaker não encontrar um texto dentro dos recursos de string do seu projeto, ele vai exibir o código de situação. Portanto, se você for criar seu próprio projeto, inicie seu código de situação a partir de 100.