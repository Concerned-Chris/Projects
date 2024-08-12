package de.fau.qLStore.support;

import org.apache.jena.sparql.algebra.walker.ElementWalker_New.EltWalker;
import org.apache.jena.sparql.expr.ExprVisitor;
import org.apache.jena.sparql.expr.ExprVisitorBase;
import org.apache.jena.sparql.syntax.*;

public class ElementDeepWalker {
    public static void walk(Element el, ElementVisitor visitor) {
        walk(el, visitor, null, null) ;
    }

    public static void walkWithService(Element el, ElementVisitor visitor) {
        EltWalker w = new DeepWalker(visitor, null, null);
        walk(el, visitor, null, null, w);
    }

    protected static void walk(Element el, ElementVisitor visitor, EltWalker w) {
        walk(el, visitor, null, null, w);
    }

    protected static void walk(Element el, ElementVisitor visitor,
                               ElementVisitor beforeVisitor, ElementVisitor afterVisitor) {
        EltWalker w = new SubQueryWalker(visitor, beforeVisitor, afterVisitor);
        walk(el, visitor, beforeVisitor, afterVisitor, w);
    }

    protected static void walk(Element el, ElementVisitor visitor,
                               ElementVisitor beforeVisitor, ElementVisitor afterVisitor,
                               EltWalker w) {
        el.visit(w);
    }

    protected static class DeepWalker extends EltWalker {
        protected DeepWalker(ElementVisitor visitor, ExprVisitor exprVisitor) {
            super(visitor, exprVisitor);
        }

        protected DeepWalker(ElementVisitor visitor,
                             ElementVisitor dummyBeforeVisitor,
                             ElementVisitor dummyAfterVisitor) {
            super(visitor, new ExprVisitorBase());
        }

        @Override
        public void visit(ElementOptional el){
            elementVisitor.visit(el);
        }

        @Override
        public void visit(ElementUnion el){
            elementVisitor.visit(el);
        }

        @Override
        public void visit(ElementSubQuery el) {
            elementVisitor.visit(el);
            ElementDeepWalker.walk(el.getQuery().getQueryPattern(), elementVisitor);
        }

        @Override
        public void visit(ElementService el) {
            elementVisitor.visit(el);
            ElementDeepWalker.walk(el.getElement(), elementVisitor);
        }
    }

    protected static class SubQueryWalker extends DeepWalker {
        protected SubQueryWalker(ElementVisitor visitor,
                                 ElementVisitor dummyBeforeVisitor,
                                 ElementVisitor dummyAfterVisitor) {
            super(visitor, new ExprVisitorBase());
        }

        @Override
        public void visit(ElementService el) {

            elementVisitor.visit(el);
        }
    }
}