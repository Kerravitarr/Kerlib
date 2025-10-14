/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package kerlib;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author ilia
 */
public class ZipBufferTest {
    
    public ZipBufferTest() {
    }

    @org.junit.jupiter.api.BeforeAll
    public static void setUpClass() throws Exception {
    }

    @org.junit.jupiter.api.AfterAll
    public static void tearDownClass() throws Exception {
    }

    @org.junit.jupiter.api.BeforeEach
    public void setUp() throws Exception {
    }

    @org.junit.jupiter.api.AfterEach
    public void tearDown() throws Exception {
    }

    @org.junit.jupiter.api.Test
    public void addInChain() {
        var buffer = ZipBuffer.asIn(2,2);
        var next = buffer.next(ZipBuffer.asIn(2,2));
        compare(buffer, 0,0);compare(next, 0,0);
        buffer.add(1);
        compare(buffer, 1,0);compare(next, 1,0);
        buffer.add(2);
        compare(buffer, 3,0);compare(next, 3,0);
        buffer.add(3);
        compare(buffer, 5,3);compare(next, 8,0);
        buffer.add(4);
        compare(buffer, 7,3);compare(next, 10,0);
        buffer.add(5);
        compare(buffer, 9,7);compare(next, 16,10);
        buffer.add(4);
        compare(buffer, 9,7);compare(next, 16,10);
        buffer.add(3);
        compare(buffer, 7,9);compare(next, 16,10);
        buffer.add(2);
        compare(buffer, 5,9);compare(next, 14,10);
    }
    
    @org.junit.jupiter.api.Test
    public void asQuet() {
        var buffer = ZipBuffer.asIn(2,1);
        compare(buffer, 0,0);
        buffer.add(1);
        compare(buffer, 1,0);
        buffer.add(2);
        compare(buffer, 2,1);
        buffer.add(100);
        compare(buffer, 100,2);
        buffer.add(0);
        compare(buffer, 0,100);
    }
    @org.junit.jupiter.api.Test
    public void summ() {
        var buffer = ZipBuffer.asIn(-1);
        var next = buffer.next(ZipBuffer.asIn(2,2));
        compare(buffer, 0);compare(next, 0,0);
        buffer.add(1);
        compare(buffer, 1);compare(next, 1,0);
        buffer.add(2);
        compare(buffer, 3);compare(next, 3,0);
        buffer.add(3);
        compare(buffer, 6);compare(next, 5,3);
        buffer.add(4);
        compare(buffer, 10);compare(next, 7,3);
        buffer.add(5);
        compare(buffer, 15);compare(next, 9,7);
        buffer.add(4);
        compare(buffer, 19);compare(next, 9,7);
        buffer.add(3);
        compare(buffer, 22);compare(next, 7,9);
        buffer.add(2);
        compare(buffer, 24);compare(next, 5,9);
    }
    @org.junit.jupiter.api.Test
    public void inf() {
        var buffer = ZipBuffer.asIn(2,2);
        var next = buffer.next(ZipBuffer.asIn(0,2));
        compare(buffer, 0,0);compare(next, 0);
        buffer.add(1);
        compare(buffer, 1,0);compare(next, 1);
        buffer.add(2);
        compare(buffer, 3,0);compare(next, 3);
        buffer.add(3);
        compare(buffer, 5,3);compare(next, 8);
        buffer.add(4);
        compare(buffer, 7,3);compare(next, 10);
        buffer.add(5);
        compare(buffer, 9,7);compare(next, 16,10);
        buffer.add(4);
        compare(buffer, 9,7);compare(next, 16,10);
        buffer.add(3);
        compare(buffer, 7,9);compare(next, 16,10);
        buffer.add(2);
        compare(buffer, 5,9);compare(next, 14,10);
        buffer.add(2);
        compare(buffer, 4,5);compare(next, 9, 14,10);
    }
    
    @org.junit.jupiter.api.Test
    public void bigChain() {
        var buffer = ZipBuffer.asIn(3,2);
        var next = buffer.next(ZipBuffer.asIn(2,2));
        compare(buffer, 0,0,0);compare(next, 0,0);
        buffer.add(1);
        compare(buffer, 1,0,0);compare(next, 1,0);
        buffer.add(2);
        compare(buffer, 3,0,0);compare(next, 3,0);
        buffer.add(3);
        compare(buffer, 5,3,0);compare(next, 8,0);
        buffer.add(4);
        compare(buffer, 7,3,0);compare(next, 10,0);
        buffer.add(5);
        compare(buffer, 9,7,3);compare(next, 16,10);
        buffer.add(4);
        compare(buffer, 9,7,3);compare(next, 16,10);
        buffer.add(3);
        compare(buffer, 7,9,7);compare(next, 16,10);
        buffer.add(2);
        compare(buffer, 5,9,7);compare(next, 14,10);
    }
    private void compare(ZipBuffer<Integer> buffer, int... values){
        var list = buffer.asList();
        assertEquals(values.length,list.size(),"Не совпадает размер массивов");
        for (int i = 0; i < values.length; i++) {
            assertEquals(values[i],list.get(i),"Не совпали значения в массивах на позиции " + i + ". Массив: " + buffer);
        }
    }
}
