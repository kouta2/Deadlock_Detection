print('client.operation(\'BEGIN\', expected=\'OK\')')
for i in range(1000):
    print('client.operation(\'SET A.' + str(i) + ' = ' + str(i) + '\', expected=\'OK\')')

print('client.operation(\'COMMIT\', expected=\'COMMIT OK\')')
