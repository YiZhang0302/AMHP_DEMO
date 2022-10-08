# 暂未完成的函数

## relayNode.checkLockMessage

## relayNode.checkUnlockMessage





# forwardMessage

## 传入参数消息格式

- 字典
  - Y
  - X
  - state
  - endTime
  - signData
  - sig
  - onionMessage
  - encPaddingLen
  - sender
- onionMessage
  - Enc_aek_key///next_pee_message+padding

## 返回参数

- 字典
  - Y
  - X
  - state
  - endTime
  - signData
  - sig
  - onionMessage
  - encPaddingLen
  - nextport
  - encPaddingLen
  - nextPaddedMessage
  - Sender

# backwardMessage

## 传入参数

- 字典
  - R
  - sig

- 



# Config.properties

- MAX_MESSAGE_LEN
- POINT_H
- SERVER_IP
- SERVER_PORT
- CLIENT_PORT
- CLIENT_IP
- SERVER_RSA_PRIVATE_KEY
- SERVER_RSA_PUBLIC_KEY
- CLIENT_RSA_PRIVATE_KEY
- CLIENT_RSA_PUBLIC_KEY
- NODE_8130_RSA_PRIVATE_KEY
- NODE_8130_RSA_PUBLIC_KEY
- RELAY_NODE_PORT_START
- G1_POINT
  - BASE64编码

- H_POINT
  - BASE64编码
- NODE_8130_SIGN_KEY_FILENAME


```python
#config file 0
SERVER_IP=127.0.0.1
CLIENT_PORT=9050
CLIENT_IP=127.0.0.1
MAX_MESSAGE_LEN=1024
RELAY_NODE_PORT_START=8100
CLIENT_SIGN_KEY_FILENAME
SERVER_SIGN_KEY_FILENAME
```



# 智能合约

```solidity
struct Partner{
	address id;
	
	uint256 balance;
}

struct State{
	uint sequence
	uint amout
	
	Partner partnerA;
	Partner partnerB;

}
```





