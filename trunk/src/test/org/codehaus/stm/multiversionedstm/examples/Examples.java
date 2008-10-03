package org.codehaus.stm.multiversionedstm.examples;

import org.codehaus.stm.multiversionedstm.MultiversionedStm;

public class Examples {
    private MultiversionedStm stm;
    private int stack1Ptr;
    private int stack2Ptr;

    /*
    public void init(){
        new TransactionTemplate(stm){
            void withinTransaction(MultiversionedTransaction t) throws Exception {
                Stack<String> stack1 = new Stack<String>();
                Stack<String> stack2 = new Stack<String>();
                t.attach(stack1);
                t.attach(stack2);
            }
        }.execute();

        //todo: retrieve ptr to stack.
    }

    public void produceOnFirst(){
        new TransactionTemplate(stm){
            void withinTransaction(MultiversionedTransaction t) throws Exception {
                Stack<String> stack1 = (Stack<String>)t.read(stack1Ptr);
                String work = "work";
                stack1.push(work);
            }
        }.execute();
    }

    public void kieperOver(){
        new TransactionTemplate(stm){
            void withinTransaction(MultiversionedTransaction t) throws Exception {
                Stack<String> stack1 = (Stack<String>)t.read(stack1Ptr);
                Stack<String> stack2 = (Stack<String>)t.read(stack2Ptr);
                String item = stack1.pop();
                stack2.push(item);
            }
        }.execute();
    }

    public void consumeOnLast(){
        new TransactionTemplate(stm){
            void withinTransaction(MultiversionedTransaction t) throws Exception {
                Stack<String> stack2 = (Stack)t.read(stack2Ptr);
                String work = stack2.pop();
                System.out.println("processing: "+work);
            }
        }.execute();
    } */
}
