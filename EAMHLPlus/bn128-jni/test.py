

def down_url(prefix, start, end):
    count = 0
    for i in range(start, end):
        count += 1
        print(prefix + str(i))
        if count % 20 ==0:
            print("          ")


if __name__ =='__main__':
    prefix = 'https://www.bilibili.com/video/BV1Rx411876f?p='
    start = 150
    end = 200
    down_url(prefix, start, end)