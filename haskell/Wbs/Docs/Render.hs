{-# LANGUAGE OverloadedStrings #-}

module Wbs.Docs.Render where

import           Control.Arrow ((&&&))

import           Data.Map (Map)
import qualified Data.Map as Map

import           Text.Blaze.Html5            ((!))
import qualified Text.Blaze.Html5            as Html5
import qualified Text.Blaze.Html5.Attributes as Attr

import           Wbs.Docs.Data (ElemDoc (..))

renderPage :: [ElemDoc] -> Html5.Html
renderPage elems = Html5.docTypeHtml $ do
	renderPageHead
	renderPageBody elems

renderPageHead :: Html5.Html
renderPageHead = Html5.head $ do

	Html5.title "WBS developers documentation"

	--Html5.link
	--	! Attr.rel "stylesheet"
	--	! Attr.href "/styles"

	Html5.link
		! Attr.href "http://fonts.googleapis.com/css?family=Montserrat:400,700|Muli:400,300,300italic,400italic"
		! Attr.rel "stylesheet"
		! Attr.type_ "text/css"

groupBy :: Ord k => (v -> k) -> [v] -> [(k, [v])]
groupBy getKey = Map.toAscList . Map.fromListWith (++) . map (getKey &&& (:[]))

renderPageBody :: [ElemDoc] -> Html5.Html
renderPageBody elems = Html5.body $ do

	Html5.h1 "WBS developers documentation"

	mapM_ renderElemType $ groupBy elemType elems

renderElemType :: (String, [ElemDoc]) -> Html5.Html
renderElemType (elemType, elems) = Html5.article $ do

	Html5.h1 . Html5.toHtml $ "Element type: " ++ elemType

	Html5.p "TODO"

	mapM_ renderElemDoc elems
	
renderElemDoc :: ElemDoc -> Html5.Html
renderElemDoc elem = Html5.article $ do

	Html5.h1 $ Html5.toHtml $ elemName elem

	Html5.p "TODO"
